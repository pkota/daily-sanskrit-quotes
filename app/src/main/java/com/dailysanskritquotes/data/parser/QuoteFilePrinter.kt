package com.dailysanskritquotes.data.parser

import com.dailysanskritquotes.data.model.Manifest
import com.dailysanskritquotes.data.model.QuoteDelta
import com.dailysanskritquotes.data.model.QuoteDto
import com.dailysanskritquotes.data.model.QuoteFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object QuoteFilePrinter {

    private val json = Json { prettyPrint = true }

    fun print(quotes: List<QuoteDto>, version: Int): String {
        val file = QuoteFile(version = version, quotes = quotes)
        return json.encodeToString(file)
    }

    fun printManifest(manifest: Manifest): String {
        return json.encodeToString(manifest)
    }

    fun printDelta(delta: QuoteDelta): String {
        return json.encodeToString(delta)
    }
}
