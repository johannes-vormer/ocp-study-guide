package learn.ocp.content

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QuestionParserTest {

    private val sample = """
        ---
        title: "Sample"
        source: test
        topic: d02-flow
        ---

        # Heading is ignored

        ## t-001

        What does this print?

        ```java
        // ## not a question heading
        System.out.println(1);
        ```

        - **A.** `1`
        - **B.** Two lines:

          ```java
          int x;
          ```
        - **C.** Does not compile

        **Answer:** A

        **Explanation:** Because.

        ```java
        - **D.** not an option
        ```

        ## t-002
        **Topic:** d03-oop

        Pick two.

        - **A.** a
        - **B.** b
        - **C.** c

        **Answer:** B, C

        **Explanation:** b and c.
    """.trimIndent()

    @Test
    fun `parses questions, options, answers and topic override`() {
        val result = QuestionParser.parse("test/sample", "practice/test", sample)
        assertEquals("Sample", result.set.title)
        assertEquals(listOf("t-001", "t-002"), result.set.questionIds)

        val (q1, q2) = result.questions
        assertEquals(listOf("A", "B", "C"), q1.options.map { it.letter })
        assertEquals(setOf("A"), q1.answers)
        assertEquals("d02-flow", q1.topic)
        assertTrue("int x;" in q1.options[1].html, "option continuation keeps code block")
        assertTrue("not an option" in q1.explanationHtml)
        assertTrue("## not a question heading" in q1.bodyHtml)

        assertEquals(setOf("B", "C"), q2.answers)
        assertTrue(q2.multi)
        assertEquals("d03-oop", q2.topic)
    }

    @Test
    fun `rejects answer that is not an option`() {
        val bad = "---\ntopic: x\n---\n## q\n\nQ?\n\n- **A.** a\n- **B.** b\n\n**Answer:** E\n"
        assertFailsWith<IllegalArgumentException> { QuestionParser.parse("s", "", bad) }
    }

    @Test
    fun `all practice files in the repository parse`() {
        val dir = Path.of("practice")
        if (!Files.isDirectory(dir)) return
        val files = Files.walk(dir).use { s -> s.filter { it.extension == "md" }.toList() }
        val ids = mutableSetOf<String>()
        files.forEach { f ->
            val r = QuestionParser.parse(f.toString(), "", f.readText())
            assertTrue(r.questions.isNotEmpty(), "$f has no questions")
            r.questions.forEach { q -> assertTrue(ids.add(q.id), "duplicate id ${q.id} in $f") }
        }
    }
}
