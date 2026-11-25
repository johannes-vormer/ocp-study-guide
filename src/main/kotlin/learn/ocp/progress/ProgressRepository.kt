package learn.ocp.progress

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Session(
    val id: Long,
    val mode: String,
    val title: String,
    val questionIds: List<String>,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val timeLimit: Int?,
) {
    val isExam get() = mode == "exam"
    val deadline: LocalDateTime? get() = timeLimit?.let { startedAt.plusMinutes(it.toLong()) }
}

data class Attempt(val questionId: String, val selected: Set<String>, val correct: Boolean) {
    val selectedText: String get() = selected.sorted().joinToString(", ")
}

data class TopicProgress(val topicId: String, val status: String, val confidence: Int, val notes: String)

/** Latest result per question: the basis for "mastered" / "wrong" statistics. */
data class LatestResult(val questionId: String, val correct: Boolean, val answeredAt: LocalDateTime, val timesSeen: Int)

data class SessionSummary(val session: Session, val answered: Int, val correct: Int)

@Repository
class ProgressRepository(private val jdbc: JdbcTemplate) {

    private fun now() = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

    // ---- sessions -------------------------------------------------------

    fun createSession(mode: String, title: String, questionIds: List<String>, timeLimit: Int?): Long {
        val keys = GeneratedKeyHolder()
        jdbc.update({ con ->
            con.prepareStatement(
                "INSERT INTO session(mode, title, question_ids, started_at, time_limit) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS,
            ).apply {
                setString(1, mode); setString(2, title); setString(3, questionIds.joinToString(","))
                setString(4, now().toString()); if (timeLimit == null) setNull(5, java.sql.Types.INTEGER) else setInt(5, timeLimit)
            }
        }, keys)
        return keys.key!!.toLong()
    }

    fun session(id: Long): Session? = jdbc.query("SELECT * FROM session WHERE id = ?", { rs, _ ->
        Session(
            id = rs.getLong("id"),
            mode = rs.getString("mode"),
            title = rs.getString("title"),
            questionIds = rs.getString("question_ids").split(',').filter { it.isNotBlank() },
            startedAt = LocalDateTime.parse(rs.getString("started_at")),
            finishedAt = rs.getString("finished_at")?.let(LocalDateTime::parse),
            timeLimit = rs.getInt("time_limit").takeIf { !rs.wasNull() },
        )
    }, id).firstOrNull()

    fun finishSession(id: Long) {
        jdbc.update("UPDATE session SET finished_at = ? WHERE id = ? AND finished_at IS NULL", now().toString(), id)
    }

    fun recentSessions(limit: Int): List<SessionSummary> =
        jdbc.queryForList("SELECT id FROM session ORDER BY id DESC LIMIT ?", Long::class.java, limit).filterNotNull()
            .mapNotNull { id ->
                val s = session(id) ?: return@mapNotNull null
                val stats = jdbc.queryForMap(
                    "SELECT COUNT(*) AS n, COALESCE(SUM(correct),0) AS c FROM attempt WHERE session_id = ?", id,
                )
                SessionSummary(s, (stats["n"] as Number).toInt(), (stats["c"] as Number).toInt())
            }

    // ---- attempts -------------------------------------------------------

    fun saveAttempt(sessionId: Long, questionId: String, selected: Set<String>, correct: Boolean) {
        jdbc.update(
            """INSERT INTO attempt(session_id, question_id, selected, correct, answered_at) VALUES (?,?,?,?,?)
               ON CONFLICT(session_id, question_id) DO UPDATE SET selected = excluded.selected,
               correct = excluded.correct, answered_at = excluded.answered_at""",
            sessionId, questionId, selected.sorted().joinToString(","), if (correct) 1 else 0, now().toString(),
        )
    }

    fun attempts(sessionId: Long): Map<String, Attempt> =
        jdbc.query("SELECT question_id, selected, correct FROM attempt WHERE session_id = ?", { rs, _ ->
            Attempt(
                rs.getString("question_id"),
                rs.getString("selected").split(',').filter { it.isNotBlank() }.toSet(),
                rs.getInt("correct") == 1,
            )
        }, sessionId).associateBy { it.questionId }

    /** Most recent attempt for every question ever answered. */
    fun latestResults(): Map<String, LatestResult> =
        jdbc.query(
            """SELECT a.question_id, a.correct, a.answered_at, x.n
               FROM attempt a
               JOIN (SELECT question_id, MAX(id) AS max_id, COUNT(*) AS n FROM attempt GROUP BY question_id) x
                 ON a.id = x.max_id""",
            { rs, _ ->
                LatestResult(
                    rs.getString(1), rs.getInt(2) == 1, LocalDateTime.parse(rs.getString(3)), rs.getInt(4),
                )
            },
        ).associateBy { it.questionId }

    /** Number of attempts per day (for the activity chart). */
    fun attemptsPerDay(days: Int): Map<String, Pair<Int, Int>> =
        jdbc.query(
            """SELECT substr(answered_at, 1, 10) AS d, COUNT(*) AS n, SUM(correct) AS c FROM attempt
               WHERE answered_at >= ? GROUP BY d ORDER BY d""",
            { rs, _ -> rs.getString(1) to (rs.getInt(2) to rs.getInt(3)) },
            LocalDateTime.now().minusDays(days.toLong()).toLocalDate().toString(),
        ).toMap()

    // ---- topic progress -------------------------------------------------

    fun topicProgress(): Map<String, TopicProgress> =
        jdbc.query("SELECT * FROM topic_progress", { rs, _ ->
            TopicProgress(rs.getString("topic_id"), rs.getString("status"), rs.getInt("confidence"), rs.getString("notes"))
        }).associateBy { it.topicId }

    fun saveTopicProgress(topicId: String, status: String, confidence: Int, notes: String) {
        jdbc.update(
            """INSERT INTO topic_progress(topic_id, status, confidence, notes, updated_at) VALUES (?,?,?,?,?)
               ON CONFLICT(topic_id) DO UPDATE SET status = excluded.status, confidence = excluded.confidence,
               notes = excluded.notes, updated_at = excluded.updated_at""",
            topicId, status, confidence.coerceIn(0, 5), notes, now().toString(),
        )
    }

    // ---- study plan -----------------------------------------------------

    fun planDaysDone(): Set<Int> = jdbc.queryForList("SELECT day_no FROM plan_day WHERE done_at IS NOT NULL", Int::class.java).filterNotNull().toSet()

    fun setPlanDay(day: Int, done: Boolean) {
        if (done) jdbc.update("INSERT OR REPLACE INTO plan_day(day_no, done_at) VALUES (?, ?)", day, now().toString())
        else jdbc.update("DELETE FROM plan_day WHERE day_no = ?", day)
    }

    // ---- settings -------------------------------------------------------

    fun setting(key: String): String? =
        jdbc.queryForList("SELECT value FROM setting WHERE key = ?", String::class.java, key).firstOrNull()

    fun saveSetting(key: String, value: String) {
        jdbc.update("INSERT OR REPLACE INTO setting(key, value) VALUES (?, ?)", key, value)
    }
}
