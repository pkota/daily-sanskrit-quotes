package com.dailysanskritquotes.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: subhashitani-enhancements, Property 5: Custom tag removal

/**
 * Validates: Requirements 7.5
 *
 * Property 5: For any custom tag that has been added to a quote, removing that tag
 * and then querying tags for the quote should return a list that does not contain
 * the removed tag name.
 */
class CustomTagRemovalPropertyTest : StringSpec({

    /** Generates valid tag names: non-blank, trimmed length 1..30 */
    val validTagArb: Arb<String> = arbitrary {
        val base = Arb.string(minSize = 1, maxSize = 30).bind()
        val result = base.trim()
        if (result.isBlank()) "tag${base.hashCode().and(0x7FFFFFFF)}" else result
    }

    val quoteIdArb: Arb<String> = Arb.string(minSize = 1, maxSize = 20)

    "adding then removing a tag results in tag absence from DAO" {
        checkAll(25, validTagArb, quoteIdArb) { tagName, quoteId ->
            val fakeDao = FakeCustomTagDao()
            val manager = CustomTagManager(fakeDao)

            // Add the tag
            manager.addTag(quoteId, tagName) shouldBe true
            val trimmed = tagName.trim()

            // Verify it was added
            fakeDao.tags.any { it.tagName == trimmed && it.quoteId == quoteId } shouldBe true

            // Remove the tag
            manager.removeTag(quoteId, trimmed)

            // Verify it was removed
            fakeDao.tags.any { it.tagName == trimmed && it.quoteId == quoteId } shouldBe false
        }
    }

    "removing a tag does not affect other tags on the same quote" {
        checkAll(25, validTagArb, validTagArb, quoteIdArb) { tag1, tag2, quoteId ->
            val fakeDao = FakeCustomTagDao()
            val manager = CustomTagManager(fakeDao)

            val trimmed1 = tag1.trim()
            val trimmed2 = tag2.trim()

            manager.addTag(quoteId, tag1)
            manager.addTag(quoteId, tag2)

            // Remove only the first tag
            manager.removeTag(quoteId, trimmed1)

            // First tag should be gone
            fakeDao.tags.any { it.tagName == trimmed1 && it.quoteId == quoteId } shouldBe false
            // Second tag should still be present (unless it was the same as first)
            if (trimmed1 != trimmed2) {
                fakeDao.tags.any { it.tagName == trimmed2 && it.quoteId == quoteId } shouldBe true
            }
        }
    }
})
