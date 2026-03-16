package com.dailysanskritquotes.data.parser

import com.dailysanskritquotes.data.model.DeltaChange
import com.dailysanskritquotes.data.model.Manifest
import com.dailysanskritquotes.data.model.QuoteDelta
import com.dailysanskritquotes.data.model.QuoteDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QuoteFilePrinterTest {

    @Test
    fun `print produces valid JSON that can be parsed back`() {
        val quotes = listOf(
            QuoteDto("q001", "धर्मो रक्षति रक्षितः", "Dharma protects those who protect it.", "Manusmriti")
        )
        val json = QuoteFilePrinter.print(quotes, version = 1)
        val result = QuoteFileParser.parseQuoteFile(json)
        assertTrue(result.isSuccess)
        val file = result.getOrThrow()
        assertEquals(1, file.version)
        assertEquals(quotes, file.quotes)
    }

    @Test
    fun `print produces pretty-printed JSON`() {
        val quotes = listOf(
            QuoteDto("q001", "text", "translation", "source")
        )
        val json = QuoteFilePrinter.print(quotes, version = 1)
        assertTrue(json.contains("\n"))
        assertTrue(json.contains("    "))
    }

    @Test
    fun `printManifest produces valid JSON that can be parsed back`() {
        val manifest = Manifest(
            version = 5,
            minDeltaVersion = 2,
            totalQuotes = 100,
            checksum = "sha256:abc",
            updatedAt = "2025-01-01T00:00:00Z"
        )
        val json = QuoteFilePrinter.printManifest(manifest)
        val result = QuoteFileParser.parseManifest(json)
        assertTrue(result.isSuccess)
        assertEquals(manifest, result.getOrThrow())
    }

    @Test
    fun `printDelta produces valid JSON that can be parsed back`() {
        val delta = QuoteDelta(
            fromVersion = 1,
            toVersion = 3,
            changes = listOf(
                DeltaChange("add", quote = QuoteDto("q100", "s", "e", "a")),
                DeltaChange("delete", quoteId = "q50")
            )
        )
        val json = QuoteFilePrinter.printDelta(delta)
        val result = QuoteFileParser.parseDelta(json)
        assertTrue(result.isSuccess)
        assertEquals(delta, result.getOrThrow())
    }
}
