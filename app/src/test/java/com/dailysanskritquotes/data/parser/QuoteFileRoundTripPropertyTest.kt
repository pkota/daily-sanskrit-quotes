package com.dailysanskritquotes.data.parser

import com.dailysanskritquotes.data.model.QuoteDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 12: Quote file round-trip (parse ↔ print)

/**
 * Validates: Requirements 7.1, 7.2, 7.4, 7.5
 *
 * Property 12: For any valid list of QuoteDto objects, printing to JSON
 * and then parsing the resulting JSON shall produce an equivalent list
 * of QuoteDto objects.
 */
class QuoteFileRoundTripPropertyTest : StringSpec({

    val quoteDtoArb: Arb<QuoteDto> = arbitrary {
        QuoteDto(
            id = Arb.string(minSize = 1, maxSize = 50).bind(),
            sanskritText = Arb.string(minSize = 1, maxSize = 200).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 200).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 100).bind()
        )
    }

    val quoteDtoListArb: Arb<List<QuoteDto>> = Arb.list(quoteDtoArb, range = 0..20)

    "printing QuoteDto list to JSON then parsing produces equivalent list" {
        checkAll(25, quoteDtoListArb, Arb.int(1..10000)) { quotes, version ->
            val json = QuoteFilePrinter.print(quotes, version)
            val result = QuoteFileParser.parseQuoteFile(json)

            result.isSuccess shouldBe true
            val parsed = result.getOrThrow()
            parsed.version shouldBe version
            parsed.quotes shouldBe quotes
        }
    }
})
