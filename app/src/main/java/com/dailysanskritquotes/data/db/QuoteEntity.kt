package com.dailysanskritquotes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val id: String,
    val sanskritText: String,
    val englishTranslation: String,
    val attribution: String,
    val isFavorite: Boolean = false,
    val favoritedAt: Long? = null,
    val transliteration: String = "",
    val tags: String = "[]"
)
