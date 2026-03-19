package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.QuoteDto
import com.dailysanskritquotes.extractor.model.QuoteFile
import com.dailysanskritquotes.extractor.model.RawQuote
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.readText

object IdAssigner {

    private val json = Json { ignoreUnknownKeys = true }

    private fun normalize(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }

    fun assignIds(quotes: List<RawQuote>, existingQuotesPath: Path? = null): List<QuoteDto> {
        if (existingQuotesPath != null) {
            return assignIdsWithPinning(quotes, existingQuotesPath)
        }

        val padWidth = maxOf(4, quotes.size.toString().length)

        return quotes.mapIndexed { index, raw ->
            val id = "q${(index + 1).toString().padStart(padWidth, '0')}"
            val transliteration = DevanagariToIast.transliterate(raw.sanskritText)
            val tags = TagExtractor.extract(raw.labels, raw.attribution, raw.englishTranslation)

            QuoteDto(
                id = id,
                sanskritText = raw.sanskritText,
                englishTranslation = raw.englishTranslation,
                attribution = raw.attribution,
                transliteration = transliteration,
                tags = tags
            )
        }
    }

    private fun assignIdsWithPinning(quotes: List<RawQuote>, existingQuotesPath: Path): List<QuoteDto> {
        // Load existing quotes and build normalized sanskritText -> QuoteDto map
        val existingJson = existingQuotesPath.readText(Charsets.UTF_8)
        val existingFile = json.decodeFromString(QuoteFile.serializer(), existingJson)
        val existingById = existingFile.quotes.associateBy { normalize(it.sanskritText) }

        // Find max existing numeric ID for assigning new IDs
        val maxExistingId = existingFile.quotes.maxOfOrNull { dto ->
            dto.id.removePrefix("q").toIntOrNull() ?: 0
        } ?: 0

        // Separate quotes into known (pinned) and new
        val pinnedQuotes = mutableListOf<QuoteDto>()
        val newQuotes = mutableListOf<RawQuote>()

        for (raw in quotes) {
            val key = normalize(raw.sanskritText)
            val existing = existingById[key]
            if (existing != null) {
                // Reuse existing ID, but update englishTranslation and other derived fields
                val transliteration = DevanagariToIast.transliterate(raw.sanskritText)
                val tags = TagExtractor.extract(raw.labels, raw.attribution, raw.englishTranslation)
                pinnedQuotes.add(
                    QuoteDto(
                        id = existing.id,
                        sanskritText = raw.sanskritText,
                        englishTranslation = raw.englishTranslation,
                        attribution = raw.attribution,
                        transliteration = transliteration,
                        tags = tags
                    )
                )
            } else {
                newQuotes.add(raw)
            }
        }

        // Sort pinned quotes by their original ID order
        pinnedQuotes.sortBy { dto ->
            dto.id.removePrefix("q").toIntOrNull() ?: 0
        }

        // Assign new sequential IDs to new quotes starting after max existing ID
        val totalCount = pinnedQuotes.size + newQuotes.size
        val padWidth = maxOf(4, totalCount.toString().length)
        var nextId = maxExistingId + 1

        val newQuoteDtos = newQuotes.map { raw ->
            val id = "q${nextId.toString().padStart(padWidth, '0')}"
            nextId++
            val transliteration = DevanagariToIast.transliterate(raw.sanskritText)
            val tags = TagExtractor.extract(raw.labels, raw.attribution, raw.englishTranslation)
            QuoteDto(
                id = id,
                sanskritText = raw.sanskritText,
                englishTranslation = raw.englishTranslation,
                attribution = raw.attribution,
                transliteration = transliteration,
                tags = tags
            )
        }

        // Return pinned quotes in original order, then new quotes appended at the end
        return pinnedQuotes + newQuoteDtos
    }
}
