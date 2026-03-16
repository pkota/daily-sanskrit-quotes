package com.dailysanskritquotes.data.parser

import com.dailysanskritquotes.data.model.Manifest
import com.dailysanskritquotes.data.model.QuoteDelta
import com.dailysanskritquotes.data.model.QuoteFile
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object QuoteFileParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseQuoteFile(jsonString: String): Result<QuoteFile> = try {
        Result.success(json.decodeFromString<QuoteFile>(jsonString))
    } catch (e: SerializationException) {
        Result.failure(IllegalArgumentException("Failed to parse QuoteFile: ${e.message}", e))
    } catch (e: IllegalArgumentException) {
        Result.failure(IllegalArgumentException("Failed to parse QuoteFile: ${e.message}", e))
    }

    fun parseManifest(jsonString: String): Result<Manifest> = try {
        Result.success(json.decodeFromString<Manifest>(jsonString))
    } catch (e: SerializationException) {
        Result.failure(IllegalArgumentException("Failed to parse Manifest: ${e.message}", e))
    } catch (e: IllegalArgumentException) {
        Result.failure(IllegalArgumentException("Failed to parse Manifest: ${e.message}", e))
    }

    fun parseDelta(jsonString: String): Result<QuoteDelta> = try {
        Result.success(json.decodeFromString<QuoteDelta>(jsonString))
    } catch (e: SerializationException) {
        Result.failure(IllegalArgumentException("Failed to parse QuoteDelta: ${e.message}", e))
    } catch (e: IllegalArgumentException) {
        Result.failure(IllegalArgumentException("Failed to parse QuoteDelta: ${e.message}", e))
    }
}
