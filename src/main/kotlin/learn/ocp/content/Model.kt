package learn.ocp.content

data class Option(val letter: String, val html: String)

data class Question(
    val id: String,
    val setId: String,
    val topic: String,
    val bodyHtml: String,
    val options: List<Option>,
    val answers: Set<String>,
    val explanationHtml: String,
) {
    val multi: Boolean get() = answers.size > 1
    val answerText: String get() = answers.sorted().joinToString(", ")
}

data class QuestionSet(
    val id: String,          // e.g. "ocp17-829/ch01-handling-date..."
    val title: String,
    val source: String,      // ocp17-829 | java25-gap | ...
    val topic: String,
    val questionIds: List<String>,
)

data class Doc(
    val id: String,          // file name without .md, e.g. "d01-values" or "study-plan"
    val title: String,
    val topic: String?,
    val order: Int,
    val html: String,
    val raw: String,
)

/** Exam domains, in exam order. */
object Topics {
    val all: LinkedHashMap<String, String> = linkedMapOf(
        "d01-values" to "D1 · Date, time, text, numbers, booleans",
        "d02-flow" to "D2 · Program flow",
        "d03-oop" to "D3 · Object-oriented principles",
        "d04-exceptions" to "D4 · Exceptions",
        "d05-collections" to "D5 · Arrays & collections",
        "d06-streams" to "D6 · Streams & lambdas",
        "d07-modules" to "D7 · Packaging & modules",
        "d08-concurrency" to "D8 · Concurrency",
        "d09-io" to "D9 · I/O & NIO.2",
        "d10-localization" to "D10 · Localization",
        "x-jdbc" to "JDBC (Java 17 exam only)",
        "exam" to "Mixed mock exams",
    )

    fun title(id: String) = all[id] ?: id
}
