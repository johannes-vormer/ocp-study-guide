package learn.ocp.progress

import learn.ocp.content.ContentRepository
import learn.ocp.content.Markdown
import learn.ocp.content.Question
import learn.ocp.content.Topics
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Stats(val total: Int, val seen: Int, val correct: Int) {
    val wrong get() = seen - correct
    val unseen get() = total - seen
    /** Accuracy on the latest attempt of each seen question. */
    val accuracy get() = if (seen == 0) null else correct * 100 / seen
    /** Share of all questions whose latest attempt is correct. */
    val mastery get() = if (total == 0) 0 else correct * 100 / total
    val seenPct get() = if (total == 0) 0 else seen * 100 / total
}

data class TopicRow(val id: String, val title: String, val hasDoc: Boolean, val progress: TopicProgress?, val stats: Stats)

data class PlanDay(val day: Int, val title: String, val html: String, val done: Boolean)

/** Which questions a new session should contain. */
data class SessionRequest(
    val scope: String,           // set:<id> | topic:<id> | source:<id> | all | mock-random
    val filter: String = "all",  // all | unseen | wrong | todo (unseen or wrong)
    val count: Int = 0,          // 0 = no limit
    val shuffle: Boolean = false,
    val mode: String = "practice",
)

@Service
class StudyService(private val content: ContentRepository, private val progress: ProgressRepository) {

    fun stats(questions: Collection<Question>, latest: Map<String, LatestResult> = progress.latestResults()): Stats {
        val results = questions.mapNotNull { latest[it.id] }
        return Stats(questions.size, results.size, results.count { it.correct })
    }

    fun topicRows(): List<TopicRow> {
        val latest = progress.latestResults()
        val tp = progress.topicProgress()
        val docTopics = content.topicDocs().mapNotNull { it.topic }.toSet()
        return Topics.all.map { (id, title) ->
            TopicRow(id, title, id in docTopics, tp[id], stats(content.questionsForTopic(id), latest))
        }
    }

    fun overall(): Stats = stats(content.questions.values)

    // ---- exam date ------------------------------------------------------

    fun examDate(): LocalDate? = progress.setting("exam-date")?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun daysLeft(): Long? = examDate()?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }

    // ---- study plan (docs/study-plan.md, "### Day N — title" sections) --

    fun planDays(): List<PlanDay> {
        val raw = content.docs["study-plan"]?.raw ?: return emptyList()
        val done = progress.planDaysDone()
        val heading = Regex("""^### Day (\d+)\s*[—–-]?\s*(.*)$""")
        val days = mutableListOf<PlanDay>()
        var current: Pair<Int, String>? = null
        val buf = StringBuilder()
        fun flush() {
            current?.let { (n, t) -> days += PlanDay(n, t, Markdown.render(buf.toString(), "docs"), n in done) }
            buf.clear()
        }
        for (line in raw.lines()) {
            val m = heading.find(line)
            when {
                m != null -> { flush(); current = m.groupValues[1].toInt() to m.groupValues[2] }
                line.startsWith("## ") || line.startsWith("# ") -> { flush(); current = null }
                current != null -> buf.append(line).append('\n')
            }
        }
        flush()
        return days
    }

    // ---- sessions -------------------------------------------------------

    fun selectQuestions(req: SessionRequest): Pair<String, List<Question>> {
        val all = content.questions.values
        val (title, pool) = when {
            req.scope.startsWith("set:") -> {
                val set = content.sets[req.scope.removePrefix("set:")] ?: error("Unknown set")
                set.title to set.questionIds.mapNotNull { content.questions[it] }
            }
            req.scope.startsWith("topic:") -> {
                val t = req.scope.removePrefix("topic:")
                Topics.title(t) to content.questionsForTopic(t)
            }
            req.scope.startsWith("source:") -> {
                val s = req.scope.removePrefix("source:")
                val setIds = content.sets.values.filter { it.source == s }.map { it.id }.toSet()
                s to all.filter { it.setId in setIds }
            }
            req.scope == "mock-random" -> "Random mock exam (Java 25 mix)" to mockExamPool()
            else -> "All questions" to all.toList()
        }
        val latest = progress.latestResults()
        val filtered = when (req.filter) {
            "unseen" -> pool.filter { it.id !in latest }
            "wrong" -> pool.filter { latest[it.id]?.correct == false }
            "todo" -> pool.filter { latest[it.id]?.correct != true }
            else -> pool
        }
        val ordered = if (req.shuffle || req.scope == "mock-random") filtered.shuffled() else filtered
        val limited = if (req.count > 0) ordered.take(req.count) else ordered
        val suffix = when (req.filter) { "unseen" -> " · unseen"; "wrong" -> " · mistakes"; "todo" -> " · not mastered"; else -> "" }
        return (title + suffix) to limited
    }

    /**
     * 50 questions roughly following the 1Z0-831 domain weights; ~1 in 5 from the Java 25 gap sets.
     * JDBC is excluded (not on the Java 25 exam).
     */
    private fun mockExamPool(): List<Question> {
        val weights = linkedMapOf(
            "d01-values" to 5, "d02-flow" to 5, "d03-oop" to 9, "d04-exceptions" to 4, "d05-collections" to 4,
            "d06-streams" to 5, "d07-modules" to 4, "d08-concurrency" to 5, "d09-io" to 5, "d10-localization" to 4,
        )
        val gapSets = content.sets.values.filter { it.source == "java25-gap" }.map { it.id }.toSet()
        val result = mutableListOf<Question>()
        for ((topic, n) in weights) {
            val pool = content.questionsForTopic(topic)
            val gap = pool.filter { it.setId in gapSets }.shuffled()
            val classic = pool.filter { it.setId !in gapSets }.shuffled()
            val fromGap = minOf(gap.size, (n + 2) / 4)
            result += gap.take(fromGap) + classic.take(n - fromGap)
        }
        return result
    }

    fun startSession(req: SessionRequest): Long? {
        val (title, questions) = selectQuestions(req)
        if (questions.isEmpty()) return null
        val timeLimit = if (req.mode == "exam") maxOf(5, questions.size * 120 / 50) else null
        return progress.createSession(req.mode, title, questions.map { it.id }, timeLimit)
    }
}
