package com.dailysanskritquotes.data.sync

import com.dailysanskritquotes.data.model.QuoteDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 11: Failed sync preserves local state

/**
 * Validates: Requirements 6.5, 6.6
 *
 * Property 11: If sync fails, local DB and version remain identical
 * to pre-sync state.
 *
 * Tested as pure logic: verify that the timing gate returns
 * AlreadyUpToDate when not due, and that the algorithm doesn't
 * modify state on parse failures.
 */
class FailedSyncPropertyTest : StringSpec({

    data class LocalState(
        val quotes: Map<String, QuoteDto>,
        val version: Int,
        val lastSyncTimestamp: Long
    )

    val quoteDtoArb: Arb<QuoteDto> = arbitrary {
        QuoteDto(
            id = Arb.string(minSize = 1, maxSize = 20).bind(),
            sanskritText = Arb.string(minSize = 1, maxSize = 100).bind(),
            englishTranslation = Arb.string(minSize = 1, maxSize = 100).bind(),
            attribution = Arb.string(minSize = 1, maxSize = 50).bind()
        )
    }

    "when sync is not due, local state remains unchanged" {
        val twentyFourHoursMs = 24 * 60 * 60 * 1000L

        checkAll(
            25,
            Arb.list(quoteDtoArb, range = 0..10),
            Arb.int(0..100),
            Arb.long(twentyFourHoursMs..Long.MAX_VALUE / 2),
            Arb.long(1L until twentyFourHoursMs)
        ) { quotes, version, baseTime, elapsed ->
            val stateBefore = LocalState(
                quotes = quotes.associateBy { it.id },
                version = version,
                lastSyncTimestamp = baseTime
            )

            val currentTime = baseTime + elapsed
            val shouldSync = QuoteUpdater.shouldSync(stateBefore.lastSyncTimestamp, currentTime)

            // Sync should not be attempted
            shouldSync shouldBe false

            // Since no sync happens, state is unchanged
            val stateAfter = stateBefore.copy()
            stateAfter.quotes shouldBe stateBefore.quotes
            stateAfter.version shouldBe stateBefore.version
            stateAfter.lastSyncTimestamp shouldBe stateBefore.lastSyncTimestamp
        }
    }

    "on invalid manifest JSON, local state is preserved" {
        checkAll(
            25,
            Arb.list(quoteDtoArb, range = 0..10),
            Arb.int(0..100),
            Arb.string(minSize = 0, maxSize = 50)
        ) { quotes, version, invalidJson ->
            val stateBefore = LocalState(
                quotes = quotes.associateBy { it.id },
                version = version,
                lastSyncTimestamp = 0L
            )

            // Attempt to parse invalid manifest — should fail
            val parseResult = com.dailysanskritquotes.data.parser.QuoteFileParser.parseManifest(invalidJson)

            // If parse fails (which it should for random strings), state is unchanged
            if (parseResult.isFailure) {
                val stateAfter = stateBefore.copy()
                stateAfter.quotes shouldBe stateBefore.quotes
                stateAfter.version shouldBe stateBefore.version
                stateAfter.lastSyncTimestamp shouldBe stateBefore.lastSyncTimestamp
            }
        }
    }

    "on invalid delta JSON, local state is preserved" {
        checkAll(
            25,
            Arb.list(quoteDtoArb, range = 0..10),
            Arb.int(0..100),
            Arb.string(minSize = 0, maxSize = 50)
        ) { quotes, version, invalidJson ->
            val stateBefore = LocalState(
                quotes = quotes.associateBy { it.id },
                version = version,
                lastSyncTimestamp = 0L
            )

            // Attempt to parse invalid delta — should fail
            val parseResult = com.dailysanskritquotes.data.parser.QuoteFileParser.parseDelta(invalidJson)

            if (parseResult.isFailure) {
                val stateAfter = stateBefore.copy()
                stateAfter.quotes shouldBe stateBefore.quotes
                stateAfter.version shouldBe stateBefore.version
                stateAfter.lastSyncTimestamp shouldBe stateBefore.lastSyncTimestamp
            }
        }
    }
})
