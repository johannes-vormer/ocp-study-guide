package learn.ocp.content

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.AttributeProvider
import org.commonmark.renderer.html.HtmlRenderer

/** Markdown -> HTML. Relative image paths are rewritten to /content/<baseDir>/... */
object Markdown {
    private val extensions = listOf(TablesExtension.create(), TaskListItemsExtension.create())
    private val parser = Parser.builder().extensions(extensions).build()

    fun render(markdown: String, baseDir: String = ""): String {
        val renderer = HtmlRenderer.builder()
            .extensions(extensions)
            .attributeProviderFactory {
                AttributeProvider { node, _, attrs ->
                    if (node is Link) {
                        val href = attrs["href"] ?: return@AttributeProvider
                        // link between docs: "exam-objectives.md" / "topics/d01-values.md" -> in-app viewer
                        if (!href.contains("://") && href.substringBefore('#').endsWith(".md")) {
                            val name = href.substringBefore('#').substringAfterLast('/').removeSuffix(".md")
                            attrs["href"] = "/study/$name" + (href.substringAfter('#', "").let { if (it.isEmpty()) "" else "#$it" })
                        }
                    }
                    if (node is Image) {
                        val src = attrs["src"] ?: return@AttributeProvider
                        if (!src.startsWith("http") && !src.startsWith("/")) {
                            attrs["src"] = "/content/" + (if (baseDir.isEmpty()) "" else "$baseDir/") + src
                        }
                        attrs["loading"] = "lazy"
                    }
                }
            }
            .build()
        return renderer.render(parser.parse(markdown))
    }
}

/** Splits `---\nkey: value\n---` front matter from the body. */
object FrontMatter {
    fun split(text: String): Pair<Map<String, String>, String> {
        val normalized = text.replace("\r\n", "\n")
        if (!normalized.startsWith("---\n")) return emptyMap<String, String>() to normalized
        val end = normalized.indexOf("\n---", 4)
        if (end < 0) return emptyMap<String, String>() to normalized
        val meta = normalized.substring(4, end).lines()
            .mapNotNull { line ->
                val i = line.indexOf(':')
                if (i <= 0) null
                else line.substring(0, i).trim() to line.substring(i + 1).trim().removeSurrounding("\"")
            }.toMap()
        val bodyStart = normalized.indexOf('\n', end + 1).let { if (it < 0) normalized.length else it + 1 }
        return meta to normalized.substring(bodyStart)
    }
}
