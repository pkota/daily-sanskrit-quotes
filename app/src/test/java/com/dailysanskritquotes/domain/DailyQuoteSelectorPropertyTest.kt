package com.dailysanskritquotes.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import java.time.LocalDate

// Feature: daily-sanskrit-quotes, Property 1: Daily quote determinism

/**
 * Validates: Requirements 1.1, 1.3
 *
 * Property 1: For any date and non-empty list, calling selectQuoteIndex(date, listSize)
 * multiple times returns the same index.
 */
class DailyQuoteDeterminismPropertyTest : StringSpec({

    val dateArb: Arb<LocalDate> = arbitrary {
        val epochDay = Arb.long(0L..365_000L).bind() // covers ~1000 years
        LocalDate.ofEpochDay(epochDay)
    }

    "selectQuoteIndex returns the same index for the same date and list size" {
        checkAll(25, dateArb, Arb.int(1..1000)) { date, listSize ->
            val first = DailyQuoteSelector.selectQuoteIndex(date, listSize)
            val second = DailyQuoteSelector.selectQuoteIndex(date, listSize)
            val third = DailyQuoteSelector.selectQuoteIndex(date, listSize)

            first shouldBe second
            second shouldBe third

            // Also verify index is in valid range
            (first in 0 until listSize) shouldBe true
        }
    }
})

// Feature: daily-sanskrit-quotes, Property 2: Quote uniqueness within a display cycle

/**
 * Validates: Requirements 1.4, 1.5
 *
 * Property 2: For a list of size N, simulating the cycle algorithm (select from
 * shrinking candidate list, remove selected) for N consecutive days produces
 * N distinct selections. After all N are shown, the cycle resets and the next
 * selection succeeds.
 *
 * This mirrors how DailyQuoteSelector.getQuoteForDate works: it selects from
 * unshown quotes, removes the selected one from the pool, and resets when empty.
 */
class QuoteUniquenessPropertyTest : StringSpec({

    val startDateArb: Arb<LocalDate> = arbitrary {
        val epochDay = Arb.long(0L..365_000L).bind()
        LocalDate.ofEpochDay(epochDay)
    }

    "simulated cycle of N days over N quotes produces N distinct selections and resets" {
        checkAll(25, startDateArb, Arb.int(1..50)) { startDate, n ->
            // Simulate quote IDs as 0 until n
            val allQuoteIds = (0 until n).toList()
            val remaining = allQuoteIds.toMutableList()
            val selected = mutableSetOf<Int>()

            // Select N quotes over N consecutive days
            for (dayOffset in 0 until n) {
                val date = startDate.plusDays(dayOffset.toLong())
                val index = DailyQuoteSelector.selectQuoteIndex(date, remaining.size)
                val sortedRemaining = remaining.sorted()
                val pick = sortedRemaining[index]
                selected.add(pick)
                remaining.remove(pick)
            }

            // All N quotes should have been selected (all distinct)
            selected.size shouldBe n

            // After cycle exhaustion, remaining is empty — reset and select again
            remaining.addAll(allQuoteIds)
            val nextDate = startDate.plusDays(n.toLong())
            val resetIndex = DailyQuoteSelector.selectQuoteIndex(nextDate, remaining.size)
            (resetIndex in 0 until n) shouldBe true
        }
    }
})
