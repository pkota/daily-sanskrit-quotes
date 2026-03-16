package com.dailysanskritquotes.data

import com.dailysanskritquotes.data.model.QuoteDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Feature: subhashitani-enhancements, Property 2: QuoteDto serialization round-trip

/**
 * Validates: Requirements 5.1, 5.2, 6.1, 6.2, 6.4
 *
 * Property 2: For any valid QuoteDto (including transliteration and tags fields),
 * serializing to JSON and deserializing back should produce an equivalent QuoteDto.
 */
class QuoteDtoSerializationPropertyTest : StringSpec({

    val json = Json { ignoreUnknownKeys = true }

    val quoteDtoArb: Arb<QuoteDto> = arbitrary {
        QuoteDto(
            id = Arb.string(minSize = 1, maxSize = 50).bind(),
            sanskritText = Arb.string(minSize = 1, maxSize = 200).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 200).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 100).bind(),
            transliteration = Arb.string(minSize = 0, maxSize = 200).bind(),
            tags = Arb.list(Arb.string(minSize = 1, maxSize = 30), range = 0..5).bind()
        )
    }

    "QuoteDto serialization round-trip preserves all fields" {
        checkAll(25, quoteDtoArb) { dto ->
            val serialized = json.encodeToString(dto)
            val deserialized = json.decodeFromString<QuoteDto>(serialized)
            deserialized shouldBe dto
        }
    }

    "backward compatibility: JSON without transliteration and tags deserializes with defaults" {
        checkAll(25, Arb.string(minSize = 1, maxSize = 50), Arb.string(minSize = 1, maxSize = 200)) { id, text ->
            val jsonString = """
                {
                    "id": ${json.encodeToString(id)},
                    "sanskritText": ${json.encodeToString(text)},
                    "englishTranslation": "translation",
                    "attribution": "author"
                }
            """.trimIndent()

            val deserialized = json.decodeFromString<QuoteDto>(jsonString)
            deserialized.id shouldBe id
            deserialized.sanskritText shouldBe text
            deserialized.transliteration shouldBe ""
            deserialized.tags shouldBe emptyList()
        }
    }
})
