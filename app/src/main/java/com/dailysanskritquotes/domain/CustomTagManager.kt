package com.dailysanskritquotes.domain

import com.dailysanskritquotes.data.db.CustomTagDao
import com.dailysanskritquotes.data.db.CustomTagEntity
import kotlinx.coroutines.flow.Flow

class CustomTagManager(private val customTagDao: CustomTagDao) {

    /**
     * Returns a Flow of all custom tags associated with the given quote.
     */
    fun getTagsForQuote(quoteId: String): Flow<List<CustomTagEntity>> =
        customTagDao.getTagsForQuote(quoteId)

    /**
     * Returns a Flow of all distinct custom tag names, sorted alphabetically.
     */
    fun getAllTagNames(): Flow<List<String>> =
        customTagDao.getAllDistinctTagNames()

    /**
     * Adds a custom tag to a quote.
     * Returns false if the tag name is blank or exceeds 30 characters.
     * Duplicate (tagName, quoteId) pairs are silently ignored.
     */
    suspend fun addTag(quoteId: String, tagName: String): Boolean {
        val trimmed = tagName.trim()
        if (trimmed.isBlank() || trimmed.length > 30) return false
        customTagDao.insert(CustomTagEntity(tagName = trimmed, quoteId = quoteId))
        return true
    }

    /**
     * Removes a custom tag from a quote.
     */
    suspend fun removeTag(quoteId: String, tagName: String) {
        customTagDao.delete(tagName, quoteId)
    }
}
