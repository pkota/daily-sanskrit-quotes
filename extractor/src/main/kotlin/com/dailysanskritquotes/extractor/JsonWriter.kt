package com.dailysanskritquotes.extractor

import com.dailysanskritquotes.extractor.model.QuoteDto
import com.dailysanskritquotes.extractor.model.QuoteFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.ExperimentalSerializationApi
import java.nio.file.Path
import kotlin.io.path.writeText

object JsonWriter {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "    "  // 4-space indent
        encodeDefaults = false      // omit transliteration="" and tags=[]
    }

    fun write(quotes: List<QuoteDto>, outputPath: Path) {
        val quoteFile = QuoteFile(version = 1, quotes = quotes)
        val jsonString = serialize(quoteFile)
        outputPath.writeText(jsonString, Charsets.UTF_8)
    }

    fun serialize(quoteFile: QuoteFile): String {
        return json.encodeToString(QuoteFile.serializer(), quoteFile)
    }
}
