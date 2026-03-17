package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.ParsedPost
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.nio.file.Path
import kotlin.io.path.readText

object HtmlParser {
    fun parse(file: Path): ParsedPost? {
        return try {
            val html = file.readText(Charsets.UTF_8)
            val doc: Document = Jsoup.parse(html)

            // Extract post title
            val title = doc.title().trim().ifEmpty {
                // Fallback: try h3.post-title or h1
                doc.selectFirst("h3.post-title, h1.post-title, .post-title")?.text()?.trim() ?: ""
            }

            if (title.isEmpty()) {
                System.err.println("WARNING: No title found in ${file.fileName}, skipping")
                return null
            }

            // Extract body text segments from post body
            val postBody = doc.selectFirst(".post-body, .entry-content, article")
            if (postBody == null) {
                System.err.println("WARNING: No post body found in ${file.fileName}, skipping")
                return null
            }

            // Get text segments - split by line breaks and block elements
            val bodyElements = postBody.html()
                .replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n")
                .let { Jsoup.parse(it).text() }
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            // Extract Blogger labels
            val labels = mutableListOf<String>()
            val labelLinks = doc.select("a[rel=tag]")
            if (labelLinks.isNotEmpty()) {
                labelLinks.forEach { link ->
                    val label = link.text().trim()
                    if (label.isNotEmpty()) {
                        labels.add(label)
                    }
                }
            }

            ParsedPost(
                title = title,
                bodyElements = bodyElements,
                labels = labels
            )
        } catch (e: Exception) {
            System.err.println("WARNING: Failed to parse ${file.fileName}: ${e.message}")
            null
        }
    }
}
