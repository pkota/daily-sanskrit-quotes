package com.dailysanskritquotes.data.parser

import com.dailysanskritquotes.data.model.Manifest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Feature: daily-sanskrit-quotes, Property 14: Manifest round-trip (parse ↔ print)

/**
 * Validates: Requirements 7.1
 *
 * Property 14: For any valid Manifest object, serializing to JSON
 * and then deserializing shall produce an equivalent Manifest object.
 */
class ManifestRoundTripPropertyTest : StringSpec({

    val manifestArb: Arb<Manifest> = arbitrary {
        Manifest(
            version = Arb.int(1..10000).bind(),
            minDeltaVersion = Arb.int(1..10000).bind(),
            totalQuotes = Arb.int(0..100000).bind(),
            checksum = Arb.string(minSize = 1, maxSize = 100).bind(),
            updatedAt = Arb.string(minSize = 1, maxSize = 50).bind()
        )
    }

    "serializing Manifest to JSON then parsing produces equivalent object" {
        checkAll(25, manifestArb) { manifest ->
            val json = Json.encodeToString(manifest)
            val result = QuoteFileParser.parseManifest(json)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe manifest
        }
    }
})
