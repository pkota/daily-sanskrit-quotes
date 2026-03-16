package com.dailysanskritquotes.domain

import com.dailysanskritquotes.data.db.QuoteDao
import com.dailysanskritquotes.data.db.QuoteEntity
import kotlinx.coroutines.flow.Flow

class FavoritesManager(
    private val quoteDao: QuoteDao
) {

    /**
     * Toggles the favorite status of a quote.
     * If currently favorited, removes favorite and clears favoritedAt.
     * If not favorited, sets favorite and records current timestamp.
     */
    suspend fun toggleFavorite(quoteId: String) {
        val currentlyFavorite = quoteDao.isFavorite(quoteId) ?: return
        if (currentlyFavorite) {
            quoteDao.updateFavoriteStatus(quoteId, isFavorite = false, favoritedAt = null)
        } else {
            quoteDao.updateFavoriteStatus(quoteId, isFavorite = true, favoritedAt = System.currentTimeMillis())
        }
    }

    /**
     * Returns a Flow of all favorited quotes, sorted by favoritedAt descending (most recent first).
     */
    fun getFavorites(): Flow<List<QuoteEntity>> {
        return quoteDao.getFavorites()
    }

    /**
     * Checks whether a quote is currently favorited.
     * Returns false if the quote doesn't exist.
     */
    suspend fun isFavorite(quoteId: String): Boolean {
        return quoteDao.isFavorite(quoteId) ?: false
    }
}
