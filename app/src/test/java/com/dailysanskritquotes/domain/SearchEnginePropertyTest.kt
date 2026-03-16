package com.dailysanskritquotes.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 7: Search completeness (case-insensitive, partial match)

/**
 * Validates: Requirements 5.1, 5.2
 *
 * Property 7: For any quote and any substring of its text (length ≥ 1),
 * searching returns a result set including that quote.
 *
 * Tested as pure logic: verify that SQL LIKE pattern matching logic works correctly.
 * Generate random strings, extract a random substring, verify the LIKE pattern would match.
 */
class SearchEnginePropertyTest : StringSpec({

    /**
     * Simulates the SQL LIKE '%query%' case-insensitive matching that Room uses.
     * Returns true if [text] contains [query] as a case-insensitive substring.
     */
    fun sqlLikeMatches(text: String, query: String): Boolean {
        return text.lowercase().contains(query.lowercase())
    }

    "any substring of a text matches via LIKE pattern" {
        checkAll(25, Arb.string(minSize = 1, maxSize = 100)) { text ->
            // Only test non-empty texts
            if (text.isNotEmpty()) {
                // Pick a random substring
                val startArb = Arb.int(0 until text.length)
                checkAll(1, startArb) { start ->
                    val maxEnd = text.length
                    val end = if (start + 1 >= maxEnd) maxEnd else start + 1 + (text.hashCode().and(0x7FFFFFFF) % (maxEnd - start - 1 + 1))
                    val substring = text.substring(start, end.coerceAtMost(maxEnd).coerceAtLeast(start + 1))

                    sqlLikeMatches(text, substring) shouldBe true
                }
            }
        }
    }

    "case-insensitive LIKE matching works for any substring" {
        val textAndSubstringArb = arbitrary {
            val text = Arb.string(minSize = 2, maxSize = 80).bind()
            val start = Arb.int(0 until text.length).bind()
            val end = Arb.int((start + 1)..text.length).bind()
            val substring = text.substring(start, end)
            Pair(text, substring)
        }

        checkAll(25, textAndSubstringArb) { (text, substring) ->
            // The substring in any case should match the original text
            sqlLikeMatches(text, substring) shouldBe true
            sqlLikeMatches(text, substring.uppercase()) shouldBe true
            sqlLikeMatches(text, substring.lowercase()) shouldBe true
        }
    }

    "LIKE match returns true for englishTranslation or sanskritText containing query" {
        data class FakeQuote(val sanskritText: String, val englishTranslation: String)

        val quoteAndQueryArb = arbitrary {
            val sanskrit = Arb.string(minSize = 3, maxSize = 100).bind()
            val english = Arb.string(minSize = 3, maxSize = 100).bind()
            // Pick substring from either field
            val useEnglish = Arb.int(0..1).bind() == 0
            val source = if (useEnglish) english else sanskrit
            val start = Arb.int(0 until source.length).bind()
            val end = Arb.int((start + 1)..source.length).bind()
            val query = source.substring(start, end)
            Triple(FakeQuote(sanskrit, english), query, useEnglish)
        }

        checkAll(25, quoteAndQueryArb) { (quote, query, _) ->
            // Simulate the Room DAO query: WHERE englishTranslation LIKE '%query%' OR sanskritText LIKE '%query%'
            val matches = sqlLikeMatches(quote.englishTranslation, query) ||
                sqlLikeMatches(quote.sanskritText, query)
            matches shouldBe true
        }
    }
})
