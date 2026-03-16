package com.dailysanskritquotes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShownQuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsShown(shownQuote: ShownQuoteEntity)

    @Query("SELECT quoteId FROM shown_quotes")
    suspend fun getShownQuoteIds(): List<String>

    @Query("SELECT quoteId FROM shown_quotes WHERE shownDate = :date")
    suspend fun getQuoteIdForDate(date: String): String?

    @Query("DELETE FROM shown_quotes")
    suspend fun resetAll()
}
