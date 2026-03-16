package com.dailysanskritquotes.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: subhashitani-enhancements, Property 6: Invalid tag name rejection

/**
 * Validates: Requirements 7.7, 7.8
 *
 * Property 6: For any string that is blank/whitespace-only OR longer than 30 characters,
 * calling CustomTagManager.addTag should return false and the tag should not be persisted.
 */
class InvalidTagRejectionPropertyTest : StringSpec({

    val quoteIdArb: Arb<String> = Arb.string(minSize = 1, maxSize = 20)

    /** Generates whitespace-only strings of various lengths */
    val whitespaceOnlyArb: Arb<String> = arbitrary {
        val length = Arb.int(0..20).bind()
        val whitespaceChars = listOf(' ', '\t', '\n', '\r')
        buildString {
            repeat(length) {
                append(whitespaceChars[Arb.int(0 until whitespaceChars.size).bind()])
            }
        }
    }

    /** Generates strings longer than 30 characters (after trim) */
    val tooLongArb: Arb<String> = arbitrary {
        // Generate a non-blank string of length 31..60
        val length = Arb.int(31..60).bind()
        val base = Arb.string(minSize = length, maxSize = length).bind()
        // Ensure it's non-blank and >30 after trim
        val result = if (base.trim().length > 30) base else "a".repeat(length)
        result
    }

    "whitespace-only tag names are rejected and not persisted" {
        checkAll(25, whitespaceOnlyArb, quoteIdArb) { tagName, quoteId ->
            val fakeDao = FakeCustomTagDao()
            val manager = CustomTagManager(fakeDao)

            val result = manager.addTag(quoteId, tagName)

            result shouldBe false
            fakeDao.tags.isEmpty() shouldBe true
        }
    }

    "tag names longer than 30 characters are rejected and not persisted" {
        checkAll(25, tooLongArb, quoteIdArb) { tagName, quoteId ->
            val fakeDao = FakeCustomTagDao()
            val manager = CustomTagManager(fakeDao)

            val result = manager.addTag(quoteId, tagName)

            result shouldBe false
            fakeDao.tags.isEmpty() shouldBe true
        }
    }
})
