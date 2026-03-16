package com.dailysanskritquotes.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "custom_tags",
    primaryKeys = ["tagName", "quoteId"],
    foreignKeys = [ForeignKey(
        entity = QuoteEntity::class,
        parentColumns = ["id"],
        childColumns = ["quoteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("quoteId")]
)
data class CustomTagEntity(
    val tagName: String,
    val quoteId: String
)
