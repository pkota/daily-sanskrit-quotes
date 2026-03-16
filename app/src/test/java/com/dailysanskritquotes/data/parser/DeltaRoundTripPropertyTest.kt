package com.dailysanskritquotes.data.parser

import com.dailysanskritquotes.data.model.DeltaChange
import com.dailysanskritquotes.data.model.QuoteDelta
import com.dailysanskritquotes.data.model.QuoteDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Feature: daily-sanskrit-quotes, Property 15: Delta file round-trip (parse ↔ print)

/**
 * Validates: Requirements 7.1
 *
 * Property 15: For any valid QuoteDelta object, serializing to JSON
 * and then deserializing shall produce an equivalent QuoteDelta object.
 */
class DeltaRoundTripPropertyTest : StringSpec({

    val quoteDtoArb: Arb<QuoteDto> = arbitrary {
        QuoteDto(
            id = Arb.string(minSize = 1, maxSize = 50).bind(),
            sanskritText = Arb.string(minSize = 1, maxSize = 200).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 200).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 100).bind()
        )
    }

    val deltaChangeArb: Arb<DeltaChange> = arbitrary {
        val action = Arb.element("add", "update", "delete").bind()
        when (action) {
            "add", "update" -> DeltaChange(
                action = action,
                quote = quoteDtoArb.bind(),
                quoteId = null
            )
            else -> DeltaChange(
                action = "delete",
                quote = null,
                quoteId = Arb.string(minSize = 1, maxSize = 50).bind()
            )
        }
    }

    val quoteDeltaArb: Arb<QuoteDelta> = arbitrary {
        QuoteDelta(
            fromVersion = Arb.int(1..10000).bind(),
            toVersion = Arb.int(1..10000).bind(),
            changes = Arb.list(deltaChangeArb, range = 0..20).bind()
        )
    }

    "serializing QuoteDelta to JSON then parsing produces equivalent object" {
        checkAll(25, quoteDeltaArb) { delta ->
            val json = Json.encodeToString(delta)
            val result = QuoteFileParser.parseDelta(json)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe delta
        }
    }
})
