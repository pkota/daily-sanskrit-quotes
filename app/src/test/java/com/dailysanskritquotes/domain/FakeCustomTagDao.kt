package com.dailysanskritquotes.domain

import com.dailysanskritquotes.data.db.CustomTagDao
import com.dailysanskritquotes.data.db.CustomTagEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * In-memory fake implementation of CustomTagDao for unit testing.
 * Stores tags in a mutable list, simulating Room behavior without Android dependencies.
 */
class FakeCustomTagDao : CustomTagDao {

    val tags = mutableListOf<CustomTagEntity>()

    override suspend fun insert(tag: CustomTagEntity) {
        // Simulate OnConflictStrategy.IGNORE: skip if duplicate exists
        val exists = tags.any { it.tagName == tag.tagName && it.quoteId == tag.quoteId }
        if (!exists) {
            tags.add(tag)
        }
    }

    override suspend fun delete(tagName: String, quoteId: String) {
        tags.removeAll { it.tagName == tagName && it.quoteId == quoteId }
    }

    override fun getTagsForQuote(quoteId: String): Flow<List<CustomTagEntity>> =
        flowOf(tags.filter { it.quoteId == quoteId })

    override fun getAllDistinctTagNames(): Flow<List<String>> =
        flowOf(tags.map { it.tagName }.distinct().sorted())

    override suspend fun searchQuoteIdsByTag(query: String): List<String> =
        tags.filter { it.tagName.contains(query, ignoreCase = true) }
            .map { it.quoteId }
            .distinct()
}
