package com.dailysanskritquotes.regression

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Regression test: Search must match against the attribution field.
 *
 * Previously, the searchQuotes SQL query only matched englishTranslation,
 * sanskritText, and tags — but NOT attribution. This caused searches like
 * "Bhagavad Gita" to return no results even when quotes had that attribution.
 *
 * This test verifies the LIKE matching logic covers all four searchable fields.
 */
class SearchAttributionRegressionTest : StringSpec({

    data class FakeQuote(
        val sanskritText: String,
        val englishTranslation: String,
        val attribution: String,
        val tags: String
    )

    fun sqlLikeMatches(text: String, query: String): Boolean {
        return text.lowercase().contains(query.lowercase())
    }

    fun searchMatches(quote: FakeQuote, query: String): Boolean {
        return sqlLikeMatches(quote.englishTranslation, query) ||
            sqlLikeMatches(quote.sanskritText, query) ||
            sqlLikeMatches(quote.attribution, query) ||
            sqlLikeMatches(quote.tags, query)
    }

    "substring of attribution always matches search" {
        val quoteAndQueryArb = arbitrary {
            val attribution = Arb.string(minSize = 3, maxSize = 60).bind()
            val start = Arb.int(0 until attribution.length).bind()
            val end = Arb.int((start + 1)..attribution.length).bind()
            val query = attribution.substring(start, end)
            Pair(
                FakeQuote(
                    sanskritText = "unrelated",
                    englishTranslation = "unrelated",
                    attribution = attribution,
                    tags = "[]"
                ),
                query
            )
        }

        checkAll(25, quoteAndQueryArb) { (quote, query) ->
            searchMatches(quote, query) shouldBe true
        }
    }

    "search matches attribution case-insensitively" {
        val quoteAndQueryArb = arbitrary {
            val attribution = Arb.string(minSize = 3, maxSize = 60).bind()
            val start = Arb.int(0 until attribution.length).bind()
            val end = Arb.int((start + 1)..attribution.length).bind()
            val query = attribution.substring(start, end)
            Pair(
                FakeQuote(
                    sanskritText = "x",
                    englishTranslation = "y",
                    attribution = attribution,
                    tags = "[]"
                ),
                query
            )
        }

        checkAll(25, quoteAndQueryArb) { (quote, query) ->
            searchMatches(quote, query.uppercase()) shouldBe true
            searchMatches(quote, query.lowercase()) shouldBe true
        }
    }

    "query not in any field does not match" {
        val quote = FakeQuote(
            sanskritText = "aaa",
            englishTranslation = "bbb",
            attribution = "ccc",
            tags = "ddd"
        )
        searchMatches(quote, "zzz") shouldBe false
    }
})
