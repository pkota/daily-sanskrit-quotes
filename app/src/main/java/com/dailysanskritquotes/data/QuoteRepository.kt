package com.dailysanskritquotes.data

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.dailysanskritquotes.data.db.QuoteDatabase
import com.dailysanskritquotes.data.db.QuoteEntity
import com.dailysanskritquotes.data.db.ShownQuoteEntity
import com.dailysanskritquotes.data.model.QuoteDelta
import com.dailysanskritquotes.data.model.QuoteDto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class QuoteRepository(
    private val database: QuoteDatabase,
    private val prefs: SharedPreferences
) {
    private val quoteDao = database.quoteDao()
    private val shownQuoteDao = database.shownQuoteDao()

    suspend fun getQuoteById(id: String): QuoteEntity? =
        quoteDao.getById(id)

    suspend fun getAllQuotes(): List<QuoteEntity> =
        quoteDao.getAll()

    suspend fun getUnshownQuotes(): List<QuoteEntity> =
        quoteDao.getUnshownQuotes()

    suspend fun markAsShown(quoteId: String, date: LocalDate) {
        shownQuoteDao.markAsShown(ShownQuoteEntity(quoteId, date.toString()))
    }

    suspend fun resetShownQuotes() {
        shownQuoteDao.resetAll()
    }

    suspend fun getQuoteShownOnDate(date: LocalDate): QuoteEntity? {
        val quoteId = shownQuoteDao.getQuoteIdForDate(date.toString()) ?: return null
        return quoteDao.getById(quoteId)
    }

    fun searchQuotes(query: String): Flow<List<QuoteEntity>> =
        quoteDao.searchQuotes(query)

    fun getLocalVersion(): Int =
        prefs.getInt(KEY_DB_VERSION, 0)

    fun setLocalVersion(version: Int) {
        prefs.edit().putInt(KEY_DB_VERSION, version).apply()
    }

    fun getLastSyncTimestamp(): Long =
        prefs.getLong(KEY_LAST_SYNC, 0L)

    fun setLastSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    suspend fun applyDelta(delta: QuoteDelta) {
        database.withTransaction {
            for (change in delta.changes) {
                when (change.action) {
                    "add" -> {
                        val dto = change.quote ?: continue
                        quoteDao.insert(
                            QuoteEntity(
                                id = dto.id,
                                sanskritText = dto.sanskritText,
                                englishTranslation = dto.englishTranslation,
                                attribution = dto.attribution
                            )
                        )
                    }
                    "update" -> {
                        val dto = change.quote ?: continue
                        quoteDao.updateQuoteFields(
                            id = dto.id,
                            sanskritText = dto.sanskritText,
                            englishTranslation = dto.englishTranslation,
                            attribution = dto.attribution
                        )
                    }
                    "delete" -> {
                        val id = change.quoteId ?: continue
                        val isFav = quoteDao.isFavorite(id) ?: false
                        if (!isFav) {
                            quoteDao.deleteById(id)
                        }
                    }
                }
            }
        }
    }

    suspend fun rebuildFromFull(quotes: List<QuoteDto>) {
        database.withTransaction {
            // Collect existing favorites before rebuild
            val existingQuotes = quoteDao.getAll()
            val favorites = existingQuotes
                .filter { it.isFavorite }
                .associate { it.id to it.favoritedAt }

            // Insert all quotes fresh (REPLACE strategy)
            val entities = quotes.map { dto ->
                val favoritedAt = favorites[dto.id]
                QuoteEntity(
                    id = dto.id,
                    sanskritText = dto.sanskritText,
                    englishTranslation = dto.englishTranslation,
                    attribution = dto.attribution,
                    isFavorite = favorites.containsKey(dto.id),
                    favoritedAt = favoritedAt
                )
            }
            quoteDao.insertAll(entities)
        }
    }

    companion object {
        private const val KEY_DB_VERSION = "db_version"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }
}
