package com.dailysanskritquotes.data.parser

import com.dailysanskritquotes.data.model.DeltaChange
import com.dailysanskritquotes.data.model.Manifest
import com.dailysanskritquotes.data.model.QuoteDelta
import com.dailysanskritquotes.data.model.QuoteDto
import com.dailysanskritquotes.data.model.QuoteFile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QuoteFileParserTest {

    @Test
    fun `parseQuoteFile returns success for valid JSON`() {
        val json = """
            {
                "version": 1,
                "quotes": [
                    {
                        "id": "q001",
                        "sanskritText": "धर्मो रक्षति रक्षितः",
                        "englishTranslation": "Dharma protects those who protect it.",
                        "attribution": "Manusmriti"
                    }
                ]
            }
        """.trimIndent()

        val result = QuoteFileParser.parseQuoteFile(json)
        assertTrue(result.isSuccess)
        val file = result.getOrThrow()
        assertEquals(1, file.version)
        assertEquals(1, file.quotes.size)
        assertEquals("q001", file.quotes[0].id)
    }

    @Test
    fun `parseQuoteFile returns failure for invalid JSON`() {
        val result = QuoteFileParser.parseQuoteFile("not valid json")
        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message ?: ""
        assertTrue(message.isNotEmpty())
    }

    @Test
    fun `parseQuoteFile returns failure for missing required fields`() {
        val json = """{"version": 1}"""
        val result = QuoteFileParser.parseQuoteFile(json)
        assertTrue(result.isFailure)
    }

    @Test
    fun `parseQuoteFile ignores unknown keys`() {
        val json = """
            {
                "version": 1,
                "unknownField": "hello",
                "quotes": []
            }
        """.trimIndent()

        val result = QuoteFileParser.parseQuoteFile(json)
        assertTrue(result.isSuccess)
        assertEquals(QuoteFile(version = 1, quotes = emptyList()), result.getOrThrow())
    }

    @Test
    fun `parseManifest returns success for valid JSON`() {
        val json = """
            {
                "version": 5,
                "minDeltaVersion": 2,
                "totalQuotes": 5120,
                "checksum": "sha256:abc123",
                "updatedAt": "2025-07-15T10:30:00Z"
            }
        """.trimIndent()

        val result = QuoteFileParser.parseManifest(json)
        assertTrue(result.isSuccess)
        val manifest = result.getOrThrow()
        assertEquals(5, manifest.version)
        assertEquals(2, manifest.minDeltaVersion)
        assertEquals(5120, manifest.totalQuotes)
    }

    @Test
    fun `parseManifest returns failure for invalid JSON`() {
        val result = QuoteFileParser.parseManifest("{}")
        assertTrue(result.isFailure)
    }

    @Test
    fun `parseDelta returns success for valid JSON`() {
        val json = """
            {
                "fromVersion": 3,
                "toVersion": 5,
                "changes": [
                    {
                        "action": "add",
                        "quote": {
                            "id": "q5001",
                            "sanskritText": "text",
                            "englishTranslation": "translation",
                            "attribution": "source"
                        }
                    },
                    {
                        "action": "delete",
                        "quoteId": "q999"
                    }
                ]
            }
        """.trimIndent()

        val result = QuoteFileParser.parseDelta(json)
        assertTrue(result.isSuccess)
        val delta = result.getOrThrow()
        assertEquals(3, delta.fromVersion)
        assertEquals(5, delta.toVersion)
        assertEquals(2, delta.changes.size)
        assertEquals("add", delta.changes[0].action)
        assertEquals("delete", delta.changes[1].action)
    }

    @Test
    fun `parseDelta returns failure for invalid JSON`() {
        val result = QuoteFileParser.parseDelta("[]")
        assertTrue(result.isFailure)
    }
}
