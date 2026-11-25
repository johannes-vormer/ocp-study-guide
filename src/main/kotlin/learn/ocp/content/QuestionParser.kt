package learn.ocp.content

/**
 * Parses a question file in the format described in docs/CONTENT-FORMAT.md.
 * Throws [IllegalArgumentException] with the question id when a question is malformed.
 */
object QuestionParser {
    private val optionStart = Regex("""^- \*\*([A-H])\.\*\*\s?(.*)$""")
    private val answerLine = Regex("""^\*\*Answer:\*\*\s*(.+)$""")
    private val explanationLine = Regex("""^\*\*Explanation:\*\*\s?(.*)$""")
    private val topicLine = Regex("""^\*\*Topic:\*\*\s*(\S+)\s*$""")

    data class Result(val set: QuestionSet, val questions: List<Question>)

    fun parse(setId: String, baseDir: String, text: String): Result {
        val (meta, body) = FrontMatter.split(text)
        val fileTopic = meta["topic"] ?: "exam"
        val blocks = splitQuestions(body)
        val questions = blocks.map { (id, lines) -> parseQuestion(id, lines, setId, fileTopic, baseDir) }
        val set = QuestionSet(
            id = setId,
            title = meta["title"] ?: setId,
            source = meta["source"] ?: setId.substringBefore('/'),
            topic = fileTopic,
            questionIds = questions.map { it.id },
        )
        return Result(set, questions)
    }

    /** Splits on `## id` headings, ignoring headings inside fenced code blocks. */
    private fun splitQuestions(body: String): List<Pair<String, List<String>>> {
        val result = mutableListOf<Pair<String, MutableList<String>>>()
        var inFence = false
        for (line in body.lines()) {
            if (line.trimStart().startsWith("```")) inFence = !inFence
            if (!inFence && line.startsWith("## ")) {
                result += line.removePrefix("## ").trim() to mutableListOf()
            } else {
                result.lastOrNull()?.second?.add(line)
            }
        }
        return result
    }

    private fun parseQuestion(id: String, lines: List<String>, setId: String, fileTopic: String, baseDir: String): Question {
        val body = StringBuilder()
        val options = mutableListOf<Pair<String, StringBuilder>>()
        val explanation = StringBuilder()
        var answers: Set<String>? = null
        var topic = fileTopic
        var state = "body"
        var inFence = false

        for (line in lines) {
            val fenceToggle = line.trimStart().startsWith("```")
            if (!inFence) {
                val opt = optionStart.find(line)
                val ans = answerLine.find(line)
                val exp = explanationLine.find(line)
                val top = topicLine.find(line)
                when {
                    top != null && state != "explanation" -> { topic = top.groupValues[1]; continue }
                    opt != null && state != "explanation" -> {
                        state = "option"
                        options += opt.groupValues[1] to StringBuilder(opt.groupValues[2]).append('\n')
                        continue
                    }
                    ans != null && state != "explanation" -> {
                        answers = ans.groupValues[1].split(',').map { it.trim().uppercase() }.filter { it.isNotEmpty() }.toSet()
                        state = "afterAnswer"
                        continue
                    }
                    exp != null -> {
                        state = "explanation"
                        explanation.append(exp.groupValues[1]).append('\n')
                        continue
                    }
                }
            }
            if (fenceToggle) inFence = !inFence
            when (state) {
                "body" -> body.append(line).append('\n')
                "option" -> options.last().second.append(line.removePrefix("  ")).append('\n')
                "explanation" -> explanation.append(line).append('\n')
            }
        }

        require(options.size >= 2) { "Question $id in $setId: needs at least 2 options" }
        val letters = options.map { it.first }
        require(answers != null && answers.isNotEmpty()) { "Question $id in $setId: missing **Answer:**" }
        require(letters.containsAll(answers)) { "Question $id in $setId: answer $answers not among options $letters" }

        return Question(
            id = id,
            setId = setId,
            topic = topic,
            bodyHtml = Markdown.render(body.toString().trim(), baseDir),
            options = options.map { (l, sb) -> Option(l, Markdown.render(sb.toString().trim(), baseDir)) },
            answers = answers,
            explanationHtml = Markdown.render(explanation.toString().trim(), baseDir),
        )
    }
}
