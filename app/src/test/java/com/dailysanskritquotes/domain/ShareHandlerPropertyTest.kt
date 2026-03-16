package com.dailysanskritquotes.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 6: Share formatting contains all fields

/**
 * Validates: Requirements 4.2
 *
 * Property 6: For any Quote with non-empty sanskritText, englishTranslation,
 * and attribution, the formatted share string contains all three values as substrings.
 */
class ShareHandlerPropertyTest : StringSpec({

    data class QuoteFields(
        val sanskritText: String,
        val englishTranslation: String,
        val attribution: String
    )

    val quoteFieldsArb: Arb<QuoteFields> = arbitrary {
        QuoteFields(
            sanskritText = Arb.string(minSize = 1, maxSize = 100).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 100).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 50).bind()
        )
    }

    "formatted share string contains sanskritText, englishTranslation, and attribution" {
        checkAll(25, quoteFieldsArb) { fields ->
            val formatted = ShareHandler.formatQuoteForSharing(
                sanskritText = fields.sanskritText,
                englishTranslation = fields.englishTranslation,
                attribution = fields.attribution
            )

            formatted shouldContain fields.sanskritText
            formatted shouldContain fields.englishTranslation
            formatted shouldContain fields.attribution
        }
    }

    "formatted share string contains the app attribution line" {
        checkAll(25, quoteFieldsArb) { fields ->
            val formatted = ShareHandler.formatQuoteForSharing(
                sanskritText = fields.sanskritText,
                englishTranslation = fields.englishTranslation,
                attribution = fields.attribution
            )

            formatted shouldContain "Shared via Daily Sanskrit Quotes"
        }
    }
})
