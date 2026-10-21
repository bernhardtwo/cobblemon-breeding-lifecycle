import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReadmeTest {
    private val root = File("..")
    private val readmes = listOf("README.md", "README.es.md")

    private fun readme(name: String) = File(root, name).readText()

    private fun localLinks(text: String): List<String> =
        (Regex("""\]\(([^)\s]+)\)""").findAll(text) + Regex("""src="([^"]+)"""").findAll(text))
            .map { it.groupValues[1].substringBefore('#') }
            .filter { it.isNotEmpty() && !it.startsWith("http") }
            .toList()

    private fun h2(text: String) = text.lines().filter { it.startsWith("## ") }

    @Test
    fun `english readme links to spanish first`() =
        assertEquals("[Español](README.es.md)", readme("README.md").lines().first())

    @Test
    fun `spanish readme links to english first`() =
        assertEquals("[English](README.md)", readme("README.es.md").lines().first())

    @Test
    fun everyLocalLinkResolves() {
        for (name in readmes) for (target in localLinks(readme(name))) assertTrue(File(root, target).exists(), "$name -> $target")
    }

    @Test
    fun bothReadmesHaveTheSameSectionCount() =
        assertEquals(h2(readme("README.md")).size, h2(readme("README.es.md")).size)

    @Test
    fun `no server or network name`() {
        for (name in readmes) assertFalse(readme(name).contains("diosesmon", ignoreCase = true), name)
    }

    @Test
    fun `local link scan ignores web urls and anchors`() =
        assertEquals(
            listOf("docs/a.md", "b.png"),
            localLinks("[x](docs/a.md#top) [y](https://e.com) [z](#here) <img src=\"b.png\">"),
        )
}
