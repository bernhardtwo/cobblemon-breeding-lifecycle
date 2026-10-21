import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

/** Fails the build when any comment in the given Java or TypeScript sources is longer than [maxLength]. */
abstract class CheckCommentLength : DefaultTask() {
    @get:InputFiles
    abstract val sources: ConfigurableFileCollection

    @get:Input
    abstract val maxLength: Property<Int>

    init {
        maxLength.convention(200)
    }

    @TaskAction
    fun check() {
        val max = maxLength.get()
        val root = project.rootDir.toPath()
        val findings = sources.files.sorted().flatMap { file ->
            longComments(file.readText(), max).map { (line, length) ->
                "${root.relativize(file.toPath())}:$line: comment is $length chars (max $max)"
            }
        }
        if (findings.isNotEmpty()) {
            findings.forEach { logger.error(it) }
            throw GradleException("${findings.size} comment(s) longer than $max characters")
        }
    }
}

private data class Comment(val line: Int, val text: String, val isLine: Boolean, val endLine: Int)

/** Returns (start line, length) for each comment longer than [max], measured whole without markers. */
fun longComments(text: String, max: Int): List<Pair<Int, Int>> =
    mergeLineComments(scanComments(text))
        .map { it.line to normalizedLength(it) }
        .filter { (_, length) -> length > max }

private fun normalizedLength(comment: Comment): Int =
    comment.text
        .lines()
        .map { it.trim().removePrefix("*").trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")
        .length

private fun mergeLineComments(comments: List<Comment>): List<Comment> {
    val merged = mutableListOf<Comment>()
    for (comment in comments) {
        val last = merged.lastOrNull()
        if (last != null && last.isLine && comment.isLine && comment.line == last.endLine + 1) {
            merged[merged.lastIndex] = last.copy(text = last.text + "\n" + comment.text, endLine = comment.line)
        } else {
            merged += comment
        }
    }
    return merged
}

private fun scanComments(text: String): List<Comment> {
    val comments = mutableListOf<Comment>()
    var i = 0
    var line = 1
    var lineHasCode = false
    fun advance(count: Int) {
        repeat(count) {
            if (i < text.length && text[i] == '\n') {
                line++
                lineHasCode = false
            }
            i++
        }
    }
    fun skipQuoted(close: String) {
        advance(close.length)
        while (i < text.length && !text.startsWith(close, i)) {
            advance(if (text[i] == '\\') 2 else 1)
        }
        advance(close.length)
    }
    while (i < text.length) {
        when {
            text.startsWith("//", i) -> {
                val start = line
                val standalone = !lineHasCode
                val end = text.indexOf('\n', i).let { if (it < 0) text.length else it }
                comments += Comment(start, text.substring(i + 2, end), standalone, start)
                advance(end - i)
            }
            text.startsWith("/*", i) -> {
                val start = line
                val close = text.indexOf("*/", i + 2).let { if (it < 0) text.length else it }
                val body = text.substring(i + 2, close).removePrefix("*")
                comments += Comment(start, body, false, start)
                advance(minOf(close + 2, text.length) - i)
            }
            text.startsWith("\"\"\"", i) -> skipQuoted("\"\"\"")
            text[i] == '"' -> skipQuoted("\"")
            text[i] == '\'' -> skipQuoted("'")
            text[i] == '`' -> skipQuoted("`")
            else -> {
                if (!text[i].isWhitespace()) lineHasCode = true
                advance(1)
            }
        }
    }
    return comments
}
