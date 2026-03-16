package com.dailysanskritquotes.data

import android.content.Context
import com.dailysanskritquotes.data.db.QuoteDao
import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.data.parser.QuoteFileParser

/**
 * Imports the bundled quotes-full.json asset into the Room database on first launch.
 */
class BundledQuoteImporter(
    private val context: Context,
    private val quoteDao: QuoteDao
) {

    /**
     * Parses the bundled quotes-full.json and inserts all quotes into the database
     * if the database is currently empty (first launch).
     *
     * @return the number of quotes imported, or 0 if the database already had data.
     */
    suspend fun importIfNeeded(): Int {
        if (quoteDao.getCount() > 0) return 0

        val json = context.assets.open("quotes-full.json")
            .bufferedReader()
            .use { it.readText() }

        val quoteFile = QuoteFileParser.parseQuoteFile(json).getOrElse { return 0 }

        val entities = quoteFile.quotes.map { dto ->
            QuoteEntity(
                id = dto.id,
                sanskritText = dto.sanskritText,
                englishTranslation = dto.englishTranslation,
                attribution = dto.attribution
            )
        }

        quoteDao.insertAll(entities)
        return entities.size
    }
}
