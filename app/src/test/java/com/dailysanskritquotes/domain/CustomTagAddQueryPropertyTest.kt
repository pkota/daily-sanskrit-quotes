package com.dailysanskritquotes.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: subhashitani-enhancements, Property 4: Custom tag add/query round-trip

/**
 * Validates: Requirements 7.2, 7.3, 7.6, 7.9
 *
 * Property 4: For any valid tag name (non-blank, ≤30 chars) and any quote ID,
 * adding that tag and then querying tags for the quote should return a list
 * containing that tag name.
 */
class CustomTagAddQueryPropertyTest : StringSpec({

    /** Generates valid tag names: non-blank, trimmed length 1..30 */
    val validTagArb: Arb<String> = arbitrary {
        val base = Arb.string(minSize = 1, maxSize = 30).bind()
        // Ensure non-blank after trim by prepending a letter if needed
        val result = base.trim()
        if (result.isBlank()) "tag${base.hashCode().and(0x7FFFFFFF)}" else result
    }

    val quoteIdArb: Arb<String> = Arb.string(minSize = 1, maxSize = 20)

    "addTag returns true and tag is stored in DAO for valid tag names" {
        checkAll(25, validTagArb, quoteIdArb) { tagName, quoteId ->
            val fakeDao = FakeCustomTagDao()
            val manager = CustomTagManager(fakeDao)

            val result = manager.addTag(quoteId, tagName)

            result shouldBe true
            // Verify the tag was passed to the DAO
            val trimmed = tagName.trim()
            fakeDao.tags.any { it.tagName == trimmed && it.quoteId == quoteId } shouldBe true
        }
    }

    "addTag for multiple quotes with same tag stores all associations" {
        checkAll(25, validTagArb, quoteIdArb, quoteIdArb) { tagName, quoteId1, quoteId2 ->
            val fakeDao = FakeCustomTagDao()
            val manager = CustomTagManager(fakeDao)

            manager.addTag(quoteId1, tagName) shouldBe true
            manager.addTag(quoteId2, tagName) shouldBe true

            val trimmed = tagName.trim()
            fakeDao.tags.any { it.tagName == trimmed && it.quoteId == quoteId1 } shouldBe true
            fakeDao.tags.any { it.tagName == trimmed && it.quoteId == quoteId2 } shouldBe true
        }
    }
})
