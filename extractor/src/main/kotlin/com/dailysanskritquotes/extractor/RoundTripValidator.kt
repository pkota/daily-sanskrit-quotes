package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.QuoteFile
import kotlinx.serialization.json.Json

object RoundTripValidator {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    fun validate(jsonString: String): Result<Unit> {
        return try {
            // Parse the JSON
            val parsed = json.decodeFromString(QuoteFile.serializer(), jsonString)

            // Re-serialize
            val reserialized = JsonWriter.serialize(parsed)

            // Parse again
            val reparsed = json.decodeFromString(QuoteFile.serializer(), reserialized)

            // Compare
            if (parsed == reparsed) {
                Result.success(Unit)
            } else {
                val diffs = findDifferences(parsed, reparsed)
                Result.failure(IllegalStateException("Round-trip validation failed: $diffs"))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Round-trip validation error: ${e.message}", e))
        }
    }

    private fun findDifferences(original: QuoteFile, roundTripped: QuoteFile): String {
        val diffs = mutableListOf<String>()
        if (original.version != roundTripped.version) {
            diffs.add("version: ${original.version} != ${roundTripped.version}")
        }
        if (original.quotes.size != roundTripped.quotes.size) {
            diffs.add("quotes count: ${original.quotes.size} != ${roundTripped.quotes.size}")
        } else {
            for (i in original.quotes.indices) {
                if (original.quotes[i] != roundTripped.quotes[i]) {
                    diffs.add("quote[$i] differs: ${original.quotes[i]} != ${roundTripped.quotes[i]}")
                }
            }
        }
        return diffs.joinToString("; ")
    }
}
