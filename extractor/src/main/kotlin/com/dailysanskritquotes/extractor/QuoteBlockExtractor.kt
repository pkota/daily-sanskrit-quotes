package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.ParsedPost
import com.dailysanskritquotes.extractor.model.RawQuote

object QuoteBlockExtractor {

    // Match Devanagari letters/vowels/consonants but NOT dandas (U+0964, U+0965)
    // which also appear in Harvard-Kyoto transliteration lines
    private val DEVANAGARI_RANGE = Regex("[\u0900-\u0963\u0966-\u097F]")
    private val ATTRIBUTION_PREFIX = Regex("^[-–—]\\s*")

    fun extract(post: ParsedPost, sourceFile: String): RawQuote? {
        val elements = post.bodyElements
        if (elements.isEmpty()) return null

        // Partition elements into Devanagari and Latin blocks
        val devanagariLines = mutableListOf<String>()
        val latinLines = mutableListOf<String>()
        var passedDevanagari = false

        for (element in elements) {
            if (containsDevanagari(element)) {
                devanagariLines.add(element)
                passedDevanagari = true
            } else if (passedDevanagari) {
                latinLines.add(element)
            }
        }

        if (devanagariLines.isEmpty()) {
            System.err.println("WARNING: No Devanagari text found in $sourceFile, skipping")
            return null
        }

        // Extract Sanskrit verse (Devanagari lines excluding attribution)
        val verseLines = mutableListOf<String>()
        var devanagariAttribution: String? = null
        for (line in devanagariLines) {
            if (isAttributionLine(line)) {
                devanagariAttribution = line.replace(ATTRIBUTION_PREFIX, "").trim()
            } else {
                verseLines.add(line)
            }
        }
        val sanskritText = verseLines.joinToString("\n").trim()

        if (sanskritText.isEmpty()) {
            System.err.println("WARNING: Empty Sanskrit text in $sourceFile, skipping")
            return null
        }

        // Process Latin lines: skip HK transliteration block, find English translation and attribution
        // HK transliteration lines contain Devanagari dandas (।, ॥) or Harvard-Kyoto patterns
        // (uppercase letters mid-word like nItidoShAH, mRutyuH, etc.)
        val DANDA = Regex("[।॥]")
        val HK_PATTERN = Regex("[a-z][A-Z]") // lowercase followed by uppercase = HK convention
        val latinAttributions = mutableListOf<String>()
        val latinNonAttributions = mutableListOf<String>()

        for (line in latinLines) {
            if (isAttributionLine(line)) {
                latinAttributions.add(line.replace(ATTRIBUTION_PREFIX, "").trim())
            } else {
                latinNonAttributions.add(line)
            }
        }

        // Filter out HK transliteration lines, and strip HK prefixes from mixed lines
        val englishLines = latinNonAttributions
            .filter { line -> !DANDA.containsMatchIn(line) && !isHarvardKyoto(line) }
            .map { stripHkPrefix(it) }

        val englishTranslation = englishLines.joinToString(" ").trim()

        if (englishTranslation.isEmpty()) {
            System.err.println("WARNING: No English translation found in $sourceFile (title: ${post.title}), skipping")
            return null
        }

        // Attribution: prefer the last Latin attribution (English attribution)
        // Fall back to Devanagari attribution, then to title-based extraction
        val attribution = when {
            latinAttributions.isNotEmpty() -> latinAttributions.last()
            devanagariAttribution != null -> devanagariAttribution
            else -> extractSourceFromTitle(post.title) ?: "Unknown"
        }

        return RawQuote(
            sanskritText = sanskritText,
            attribution = attribution,
            englishTranslation = englishTranslation,
            labels = post.labels,
            sourceFile = sourceFile
        )
    }

    private fun containsDevanagari(text: String): Boolean {
        return DEVANAGARI_RANGE.containsMatchIn(text)
    }

    private fun isAttributionLine(text: String): Boolean {
        return ATTRIBUTION_PREFIX.containsMatchIn(text)
    }

    /**
     * Detects Harvard-Kyoto transliteration lines.
     * HK uses uppercase letters mid-word (e.g., nItidoShAH, mRutyuH, prIyate)
     * and Devanagari dandas. English text rarely has this pattern.
     */
    private fun isHarvardKyoto(text: String): Boolean {
        val hkMatches = Regex("[a-z][A-Z]").findAll(text).count()
        return hkMatches >= 1 || Regex("[।॥]").containsMatchIn(text)
    }

    /**
     * Strips leading Harvard-Kyoto transliteration from a mixed HK+English line.
     * Returns just the English portion, or the original text if no HK prefix detected.
     */
    private fun stripHkPrefix(text: String): String {
        // Look for a transition point: HK word (contains lowercase+uppercase) followed by
        // a capitalized English word that starts a sentence
        val words = text.split(" ")
        for (i in words.indices) {
            val word = words[i]
            // A word that starts with uppercase and has no mid-word uppercase is likely English sentence start
            if (i > 0 && word.isNotEmpty() && word[0].isUpperCase() &&
                !Regex("[a-z][A-Z]").containsMatchIn(word) &&
                word.length > 1 && word != "I") {
                // Check if preceding words had HK patterns
                val prefix = words.subList(0, i).joinToString(" ")
                if (Regex("[a-z][A-Z]").containsMatchIn(prefix) ||
                    prefix.all { it.isLetter() || it.isWhitespace() || it in ".,;:!?()" }) {
                    return words.subList(i, words.size).joinToString(" ")
                }
            }
        }
        return text
    }

    /** Extracts the source portion from a title in format "{Source} - {Topic}" */
    fun extractSourceFromTitle(title: String): String? {
        val separatorIndex = title.indexOf(" - ")
        return if (separatorIndex > 0) {
            title.substring(0, separatorIndex).trim().ifEmpty { null }
        } else {
            title.trim().ifEmpty { null }
        }
    }
}
