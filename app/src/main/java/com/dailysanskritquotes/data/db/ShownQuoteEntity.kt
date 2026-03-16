package com.dailysanskritquotes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shown_quotes")
data class ShownQuoteEntity(
    @PrimaryKey val quoteId: String,
    val shownDate: String  // ISO date: "2025-01-15"
)
