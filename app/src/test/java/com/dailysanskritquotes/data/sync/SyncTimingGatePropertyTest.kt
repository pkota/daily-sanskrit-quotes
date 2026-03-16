package com.dailysanskritquotes.data.sync

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 8: Sync timing gate

/**
 * Validates: Requirements 6.2
 *
 * Property 8: If lastSyncTimestamp < 24h ago → AlreadyUpToDate without network;
 * if ≥ 24h → attempts fetch.
 *
 * Tested as pure logic via QuoteUpdater.shouldSync(lastSyncTimestamp, currentTime).
 */
class SyncTimingGatePropertyTest : StringSpec({

    val twentyFourHoursMs = 24 * 60 * 60 * 1000L

    "shouldSync returns false when less than 24 hours have elapsed" {
        checkAll(25, Arb.long(twentyFourHoursMs..Long.MAX_VALUE / 2), Arb.long(1L until twentyFourHoursMs)) { baseTime, elapsed ->
            val lastSync = baseTime
            val currentTime = baseTime + elapsed

            QuoteUpdater.shouldSync(lastSync, currentTime) shouldBe false
        }
    }

    "shouldSync returns true when exactly 24 hours have elapsed" {
        checkAll(25, Arb.long(0L..Long.MAX_VALUE / 2)) { baseTime ->
            val currentTime = baseTime + twentyFourHoursMs

            QuoteUpdater.shouldSync(baseTime, currentTime) shouldBe true
        }
    }

    "shouldSync returns true when more than 24 hours have elapsed" {
        checkAll(25, Arb.long(0L..Long.MAX_VALUE / 4), Arb.long(1L..Long.MAX_VALUE / 4)) { baseTime, extra ->
            val currentTime = baseTime + twentyFourHoursMs + extra

            QuoteUpdater.shouldSync(baseTime, currentTime) shouldBe true
        }
    }
})
