import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommentScannerTest {
    @Test
    fun `line comment of 201 chars is reported`() =
        assertEquals(listOf(1 to 201), longComments("// " + "a".repeat(201), 200))

    @Test
    fun `comment of exactly 200 chars passes`() = assertTrue(longComments("// " + "a".repeat(200), 200).isEmpty())

    @Test
    fun `slashes inside a string are not a comment`() =
        assertTrue(longComments("var u = \"http://" + "a".repeat(300) + "\";", 200).isEmpty())

    @Test
    fun `javadoc is measured whole without markers and leading stars`() =
        assertEquals(
            listOf(1 to 201),
            longComments("/**\n * " + "a".repeat(100) + "\n * " + "b".repeat(100) + "\n */", 200),
        )

    @Test
    fun `consecutive line comments count as one comment`() =
        assertEquals(listOf(2 to 201), longComments("x();\n// " + "a".repeat(100) + "\n// " + "b".repeat(100), 200))

    @Test
    fun `comment markers inside a text block are ignored`() =
        assertTrue(longComments("var t = \"\"\"\n/* " + "a".repeat(300) + " */\n\"\"\";", 200).isEmpty())

    @Test
    fun `comment markers inside a template literal are ignored`() =
        assertTrue(longComments("const t = `// " + "a".repeat(300) + "`;", 200).isEmpty())
}
