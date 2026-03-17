package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.QuoteDto
import com.dailysanskritquotes.extractor.model.RawQuote

object IdAssigner {

    fun assignIds(quotes: List<RawQuote>): List<QuoteDto> {
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
}
