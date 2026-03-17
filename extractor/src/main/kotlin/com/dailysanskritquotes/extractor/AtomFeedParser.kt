package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.ParsedPost
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.nio.file.Path
import kotlin.io.path.readText

object AtomFeedParser {

    private const val SEPARATOR = "%%NEWLINE%%"

    /**
     * Parses a Blogger Takeout Atom XML export file and returns a list of ParsedPost objects,
     * one per blog post entry. Non-post entries are filtered out via <blogger:type>POST</blogger:type>.
     */
    fun parse(file: Path): List<ParsedPost> {
        val xml = file.readText(Charsets.UTF_8)
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        val posts = mutableListOf<ParsedPost>()

        for (entry in doc.select("entry")) {
            // Blogger Takeout format uses <blogger:type>POST</blogger:type>
            val bloggerType = entry.selectFirst("blogger|type")?.text() ?: ""
            if (!bloggerType.equals("POST", ignoreCase = true)) continue

            val title = entry.selectFirst("title")?.text()?.trim() ?: ""
            if (title.isEmpty()) continue

            // In XML parser mode, .text() returns the decoded HTML content string
            val contentEl = entry.selectFirst("content") ?: continue
            val contentHtml = contentEl.text()
            if (contentHtml.isBlank()) continue

            // Parse the decoded HTML, then extract text segments using separator approach
            val parsed = Jsoup.parse(contentHtml)
            val bodyHtml = parsed.body().html()

            val bodyElements = bodyHtml
                .replace(Regex("<br\\s*/?>"), SEPARATOR)
                .replace(Regex("</p>\\s*"), SEPARATOR)
                .replace(Regex("</div>\\s*"), SEPARATOR)
                .replace(Regex("<p\\b[^>]*>"), SEPARATOR)
                .replace(Regex("<div\\b[^>]*>"), SEPARATOR)
                .let { Jsoup.parse(it).text() }
                .split(SEPARATOR)
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            // Extract Blogger labels from <category> elements
            val labels = entry.select("category").toList()
                .mapNotNull { it.attr("term").trim().ifEmpty { null } }

            posts.add(ParsedPost(title = title, bodyElements = bodyElements, labels = labels))
        }

        println("  Atom feed ${file.fileName}: found ${posts.size} post entries")
        return posts
    }
}
