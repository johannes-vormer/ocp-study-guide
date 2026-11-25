package learn.ocp.content

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.readText

/** Loads all markdown content (docs + practice questions) into memory. */
@Component
class ContentRepository(@Value("\${ocp.content-dir:.}") contentDir: String) {
    private val log = LoggerFactory.getLogger(javaClass)
    val root: Path = Path.of(contentDir).toAbsolutePath().normalize()

    @Volatile var docs: Map<String, Doc> = emptyMap()
    @Volatile var sets: Map<String, QuestionSet> = emptyMap()
    @Volatile var questions: Map<String, Question> = emptyMap()
    @Volatile var errors: List<String> = emptyList()

    init { reload() }

    @Synchronized
    fun reload() {
        val newDocs = linkedMapOf<String, Doc>()
        val newSets = linkedMapOf<String, QuestionSet>()
        val newQuestions = linkedMapOf<String, Question>()
        val newErrors = mutableListOf<String>()

        markdownFiles(root.resolve("docs")).forEach { file ->
            val (meta, body) = FrontMatter.split(file.readText())
            val id = file.name.removeSuffix(".md")
            val title = meta["title"] ?: Regex("^# (.+)$", RegexOption.MULTILINE).find(body)?.groupValues?.get(1) ?: id
            val base = root.relativize(file.parent).toString().replace('\\', '/')
            newDocs[id] = Doc(id, title, meta["topic"], meta["order"]?.toIntOrNull() ?: 100, Markdown.render(body, base), body)
        }

        markdownFiles(root.resolve("practice")).forEach { file ->
            val rel = root.resolve("practice").relativize(file).toString().replace('\\', '/').removeSuffix(".md")
            val base = root.relativize(file.parent).toString().replace('\\', '/')
            try {
                val result = QuestionParser.parse(rel, base, file.readText())
                result.questions.forEach { q ->
                    if (newQuestions.containsKey(q.id)) newErrors += "Duplicate question id ${q.id} in $rel"
                    newQuestions[q.id] = q
                }
                newSets[rel] = result.set
            } catch (e: IllegalArgumentException) {
                newErrors += "${file.name}: ${e.message}"
            }
        }

        docs = newDocs; sets = newSets; questions = newQuestions; errors = newErrors
        log.info("Loaded {} docs, {} question sets, {} questions from {}", docs.size, sets.size, questions.size, root)
        newErrors.forEach { log.warn("Content error: {}", it) }
    }

    fun topicDocs(): List<Doc> = docs.values.filter { it.topic != null && it.topic in Topics.all }.sortedBy { it.order }

    fun questionsForTopic(topic: String): List<Question> = questions.values.filter { it.topic == topic }

    private fun markdownFiles(dir: Path): List<Path> {
        if (!dir.isDirectory()) return emptyList()
        return Files.walk(dir).use { s ->
            s.filter { Files.isRegularFile(it) && it.extension == "md" }.sorted().toList()
        }
    }
}
