package com.dailysanskritquotes.domain

import com.dailysanskritquotes.data.db.CustomTagDao
import com.dailysanskritquotes.data.db.QuoteDao
import com.dailysanskritquotes.data.db.QuoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class SearchEngine(
    private val quoteDao: QuoteDao,
    private val customTagDao: CustomTagDao
) {
    fun search(query: String): Flow<List<QuoteEntity>> {
        val textResults = quoteDao.searchQuotes(query)
        return combine(textResults, flow {
            emit(customTagDao.searchQuoteIdsByTag(query))
        }) { textList, customTagQuoteIds ->
            val byId = textList.associateBy { it.id }
            val missing = customTagQuoteIds.filter { it !in byId }
            val extras = missing.mapNotNull { quoteDao.getById(it) }
            textList + extras
        }
    }
}
