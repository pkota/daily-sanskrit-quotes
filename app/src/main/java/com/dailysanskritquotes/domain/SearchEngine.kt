package com.dailysanskritquotes.domain

import com.dailysanskritquotes.data.db.QuoteDao
import com.dailysanskritquotes.data.db.QuoteEntity
import kotlinx.coroutines.flow.Flow

class SearchEngine(private val quoteDao: QuoteDao) {
    fun search(query: String): Flow<List<QuoteEntity>> = quoteDao.searchQuotes(query)
}
