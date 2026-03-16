package com.dailysanskritquotes.data.parser

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

// Feature: daily-sanskrit-quotes, Property 13: Invalid JSON produces descriptive error

/**
 * Validates: Requirements 7.3
 *
 * Property 13: For any string that is not valid JSON or doesn't conform to the
 * QuoteFile/Manifest/QuoteDelta schema, the parser shall return a failure Result
 * with a non-empty error message (and shall not throw an unhandled exception).
 */
class InvalidJsonPropertyTest : StringSpec({

    "parseQuoteFile returns failure with non-empty message for arbitrary strings" {
        checkAll(25, Arb.string(maxSize = 200)) { input ->
            val result = QuoteFileParser.parseQuoteFile(input)
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message.shouldNotBeEmpty()
        }
    }

    "parseManifest returns failure with non-empty message for arbitrary strings" {
        checkAll(25, Arb.string(maxSize = 200)) { input ->
            val result = QuoteFileParser.parseManifest(input)
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message.shouldNotBeEmpty()
        }
    }

    "parseDelta returns failure with non-empty message for arbitrary strings" {
        checkAll(25, Arb.string(maxSize = 200)) { input ->
            val result = QuoteFileParser.parseDelta(input)
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message.shouldNotBeEmpty()
        }
    }

    "parseQuoteFile returns failure for valid JSON that does not match schema" {
        checkAll(25, Arb.string(maxSize = 100)) { value ->
            val json = """{"foo": "$value"}"""
            val result = QuoteFileParser.parseQuoteFile(json)
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message.shouldNotBeEmpty()
        }
    }

    "parseManifest returns failure for valid JSON that does not match schema" {
        checkAll(25, Arb.string(maxSize = 100)) { value ->
            val json = """{"foo": "$value"}"""
            val result = QuoteFileParser.parseManifest(json)
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message.shouldNotBeEmpty()
        }
    }

    "parseDelta returns failure for valid JSON that does not match schema" {
        checkAll(25, Arb.string(maxSize = 100)) { value ->
            val json = """{"foo": "$value"}"""
            val result = QuoteFileParser.parseDelta(json)
            result.isFailure shouldBe true
            result.exceptionOrNull()!!.message.shouldNotBeEmpty()
        }
    }
})
