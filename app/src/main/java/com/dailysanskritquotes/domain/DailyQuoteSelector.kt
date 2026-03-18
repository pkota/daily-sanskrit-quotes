package com.dailysanskritquotes.domain

import com.dailysanskritquotes.data.QuoteRepository
import com.dailysanskritquotes.data.db.QuoteEntity
import java.time.LocalDate
import kotlin.math.abs

class DailyQuoteSelector(
    private val repository: QuoteRepository
) {

    /**
     * Returns the quote for the given date. Same date always returns the same quote.
     *
     * Algorithm:
     * 1. Check if a quote was already shown for this date. If yes, return it.
     * 2. Get unshown quotes. If empty, reset cycle, then get all quotes.
     * 3. Select deterministically using the date as a seed.
     * 4. Mark the selected quote as shown for this date.
     * 5. Return the selected quote.
     */
    suspend fun getQuoteForDate(date: LocalDate): QuoteEntity {
        // Step 1: Check if already shown for this date
        val existing = repository.getQuoteShownOnDate(date)
        if (existing != null) return existing

        // Step 2: Get unshown quotes; reset cycle if all shown
        var candidates = repository.getUnshownQuotes()
        if (candidates.isEmpty()) {
            resetCycleIfNeeded()
            candidates = repository.getAllQuotes()
        }

        // Step 3: Deterministic selection using date as seed
        val index = selectQuoteIndex(date, candidates.size)
        val sortedCandidates = candidates.sortedBy { it.id }
        val selected = sortedCandidates[index]

        // Step 4: Mark as shown
        repository.markAsShown(selected.id, date)

        // Step 5: Return
        return selected
    }

    /**
     * Clears shown quotes when all have been displayed, resetting the cycle.
     */
    suspend fun resetCycleIfNeeded() {
        repository.resetShownQuotes()
    }

    /**
     * Selects and marks a new unshown quote as shown with today's date.
     * Used by the hidden "Reveal Next Quote" button to simulate a new day.
     * Uses a varied seed so it doesn't collide with the day's existing quote.
     */
    suspend fun selectNextUnshownQuote(): QuoteEntity {
        var candidates = repository.getUnshownQuotes()
        if (candidates.isEmpty()) {
            resetCycleIfNeeded()
            candidates = repository.getAllQuotes()
        }
        val today = LocalDate.now()
        // Vary the seed so repeated reveals on the same day pick different quotes
        val index = selectQuoteIndex(today.plusDays(candidates.size.toLong()), candidates.size)
        val sorted = candidates.sortedBy { it.id }
        val selected = sorted[index]
        repository.markAsShown(selected.id, today)
        return selected
    }

    companion object {
        /**
         * Pure deterministic function: given a date and a list size,
         * returns a stable index in [0, listSize).
         *
         * Uses the date's hash code as a seed, mod by list size.
         */
        fun selectQuoteIndex(date: LocalDate, listSize: Int): Int {
            require(listSize > 0) { "listSize must be positive" }
            return abs(date.toString().hashCode()) % listSize
        }
    }
}
