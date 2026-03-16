package com.dailysanskritquotes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: QuoteEntity)

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getCount(): Int

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getById(id: String): QuoteEntity?

    @Query("SELECT * FROM quotes")
    suspend fun getAll(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE id NOT IN (SELECT quoteId FROM shown_quotes)")
    suspend fun getUnshownQuotes(): List<QuoteEntity>

    @Query("""
        SELECT * FROM quotes 
        WHERE englishTranslation LIKE '%' || :query || '%' 
           OR sanskritText LIKE '%' || :query || '%'
           OR attribution LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
    """)
    fun searchQuotes(query: String): Flow<List<QuoteEntity>>

    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT isFavorite FROM quotes WHERE id = :id")
    suspend fun isFavorite(id: String): Boolean?

    @Query("SELECT favoritedAt FROM quotes WHERE id = :id")
    suspend fun getFavoritedAt(id: String): Long?

    @Query("UPDATE quotes SET sanskritText = :sanskritText, englishTranslation = :englishTranslation, attribution = :attribution WHERE id = :id")
    suspend fun updateQuoteFields(id: String, sanskritText: String, englishTranslation: String, attribution: String)

    @Query("UPDATE quotes SET isFavorite = :isFavorite, favoritedAt = :favoritedAt WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean, favoritedAt: Long?)

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY favoritedAt DESC")
    fun getFavorites(): Flow<List<QuoteEntity>>
}
