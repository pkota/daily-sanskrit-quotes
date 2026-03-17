package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.RawQuote

data class DeduplicationResult(
    val uniqueQuotes: List<RawQuote>,
    val duplicateCount: Int,
    val duplicateLog: List<String>
)

object Deduplicator {

    fun normalize(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }

    fun deduplicate(quotes: List<RawQuote>): DeduplicationResult {
        val seen = mutableMapOf<String, MutableList<RawQuote>>()

        for (quote in quotes) {
            val key = normalize(quote.sanskritText)
            seen.getOrPut(key) { mutableListOf() }.add(quote)
        }

        val uniqueQuotes = mutableListOf<RawQuote>()
        val duplicateLog = mutableListOf<String>()
        var duplicateCount = 0

        for ((_, group) in seen) {
            val first = group.first()
            if (group.size > 1) {
                // Merge tags from all occurrences, deduplicated, max 2
                val mergedLabels = group.flatMap { it.labels }.distinct().take(2)
                uniqueQuotes.add(first.copy(labels = mergedLabels))

                val dupeFiles = group.drop(1).map { it.sourceFile }
                duplicateLog.add("Duplicate of quote from ${first.sourceFile} found in: ${dupeFiles.joinToString(", ")}")
                duplicateCount += group.size - 1
            } else {
                uniqueQuotes.add(first)
            }
        }

        return DeduplicationResult(
            uniqueQuotes = uniqueQuotes,
            duplicateCount = duplicateCount,
            duplicateLog = duplicateLog
        )
    }
}
