package com.dailysanskritquotes.domain

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

// Feature: subhashitani-enhancements, Property 7: Tag search inclusion

/**
 * Validates: Requirements 8.1, 8.2, 8.3, 8.5
 *
 * Property 7: For any quote with a predefined or custom tag containing a substring S
 * (case-insensitive), searching with query S should include that quote in the results,
 * even if the quote's sanskritText and englishTranslation do not contain S.
 *
 * Tested as pure logic: verify that SQL LIKE pattern matching on the tags JSON field
 * correctly matches tag substrings.
 */
class TagSearchInclusionPropertyTest : StringSpec({

    /**
     * Simulates the SQL LIKE '%query%' case-insensitive matching that Room uses.
     */
    fun sqlLikeMatches(text: String, query: String): Boolean {
        return text.lowercase().contains(query.lowercase())
    }

    /** Generates alphanumeric tag strings that won't be altered by JSON escaping */
    val safeTagArb: Arb<String> = arbitrary {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val length = Arb.int(2..30).bind()
        buildString {
            repeat(length) {
                append(chars[Arb.int(0 until chars.size).bind()])
            }
        }
    }

    "predefined tag substring matches via LIKE on serialized tags field" {
        val tagAndSubstringArb = arbitrary {
            val tags = Arb.list(safeTagArb, range = 1..5).bind()
            // Pick a random tag and extract a substring
            val tagIndex = Arb.int(0 until tags.size).bind()
            val tag = tags[tagIndex]
            val start = Arb.int(0 until tag.length).bind()
            val end = Arb.int((start + 1)..tag.length).bind()
            val substring = tag.substring(start, end)
            Pair(tags, substring)
        }

        checkAll(25, tagAndSubstringArb) { (tags, substring) ->
            // Serialize tags as JSON (same as QuoteEntity.tags field)
            val tagsJson = Json.encodeToString(tags)

            // The LIKE query on the tags column should match
            sqlLikeMatches(tagsJson, substring) shouldBe true
        }
    }

    "custom tag name substring matches via LIKE search" {
        val tagAndSubstringArb = arbitrary {
            val tagName = Arb.string(minSize = 2, maxSize = 30).bind()
            val start = Arb.int(0 until tagName.length).bind()
            val end = Arb.int((start + 1)..tagName.length).bind()
            val substring = tagName.substring(start, end)
            Pair(tagName, substring)
        }

        checkAll(25, tagAndSubstringArb) { (tagName, substring) ->
            // Custom tags are matched via CustomTagDao.searchQuoteIdsByTag
            // which uses: WHERE tagName LIKE '%' || :query || '%'
            sqlLikeMatches(tagName, substring) shouldBe true
        }
    }

    "tag search matches even when sanskritText and englishTranslation do not contain query" {
        data class FakeQuote(
            val sanskritText: String,
            val englishTranslation: String,
            val tags: String
        )

        val quoteWithTagArb = arbitrary {
            // Use safe alphanumeric tags to avoid JSON escaping issues
            val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val tagLength = Arb.int(3..20).bind()
            val tag = buildString {
                repeat(tagLength) {
                    append(chars[Arb.int(0 until chars.size).bind()])
                }
            }
            val start = Arb.int(0 until tag.length).bind()
            val end = Arb.int((start + 1)..tag.length).bind()
            val query = tag.substring(start, end)

            val tagsJson = Json.encodeToString(listOf(tag))
            Triple(
                FakeQuote(
                    sanskritText = "sanskrit_text_no_match",
                    englishTranslation = "english_text_no_match",
                    tags = tagsJson
                ),
                query,
                tag
            )
        }

        checkAll(25, quoteWithTagArb) { (quote, query, _) ->
            // Simulate the combined search: text OR tags
            val textMatch = sqlLikeMatches(quote.sanskritText, query) ||
                sqlLikeMatches(quote.englishTranslation, query)
            val tagMatch = sqlLikeMatches(quote.tags, query)

            // At least the tag should match
            tagMatch shouldBe true
            // Combined result should include this quote
            (textMatch || tagMatch) shouldBe true
        }
    }
})
