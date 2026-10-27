import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
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

    private fun sectionLinks(text: String): List<List<String>> =
        text.split(Regex("(?m)^## ")).drop(1).map { section -> localLinks(section).map { it.replace(Regex("-es([-.])"), "-en$1") } }

    @Test
    fun `sections link the same targets in the same order`() =
        assertEquals(sectionLinks(readme("README.md")), sectionLinks(readme("README.es.md")))

    @Test
    fun `both readmes end with license and credits`() {
        for (name in readmes) {
            val last = readme(name).split(Regex("(?m)^## ")).last()
            assertEquals(listOf("LICENSE", "THIRD-PARTY-NOTICES.md"), localLinks(last), name)
        }
    }

    @Test
    fun `no server or network name in docs, root markdown or mod metadata`() {
        val files = root.listFiles { f -> f.extension == "md" }!!.toList() +
            File(root, "docs").walk().filter { it.extension == "md" } +
            File(root, "mod/src/main/resources/fabric.mod.json")
        val hits = files.filter { it.readText().contains("diosesmon", ignoreCase = true) }.map { it.relativeTo(root).path }
        assertEquals(emptyList<String>(), hits)
    }

    @Test
    fun `local link scan ignores web urls and anchors`() =
        assertEquals(
            listOf("docs/a.md", "b.png"),
            localLinks("[x](docs/a.md#top) [y](https://e.com) [z](#here) <img src=\"b.png\">"),
        )
}
