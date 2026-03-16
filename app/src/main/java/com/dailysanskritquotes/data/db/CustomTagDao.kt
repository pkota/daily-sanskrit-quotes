package com.dailysanskritquotes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: CustomTagEntity)

    @Query("DELETE FROM custom_tags WHERE tagName = :tagName AND quoteId = :quoteId")
    suspend fun delete(tagName: String, quoteId: String)

    @Query("SELECT * FROM custom_tags WHERE quoteId = :quoteId")
    fun getTagsForQuote(quoteId: String): Flow<List<CustomTagEntity>>

    @Query("SELECT DISTINCT tagName FROM custom_tags ORDER BY tagName")
    fun getAllDistinctTagNames(): Flow<List<String>>

    @Query("SELECT DISTINCT quoteId FROM custom_tags WHERE tagName LIKE '%' || :query || '%'")
    suspend fun searchQuoteIdsByTag(query: String): List<String>
}
