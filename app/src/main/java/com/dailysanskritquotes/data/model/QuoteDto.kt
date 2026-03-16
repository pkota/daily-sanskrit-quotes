package com.dailysanskritquotes.data.model

import com.dailysanskritquotes.data.db.QuoteEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class QuoteDto(
    val id: String,
    val sanskritText: String,
    val englishTranslation: String,
    val attribution: String,
    val transliteration: String = "",
    val tags: List<String> = emptyList()
)

fun QuoteDto.toEntity(
    isFavorite: Boolean = false,
    favoritedAt: Long? = null
): QuoteEntity = QuoteEntity(
    id = id,
    sanskritText = sanskritText,
    englishTranslation = englishTranslation,
    attribution = attribution,
    isFavorite = isFavorite,
    favoritedAt = favoritedAt,
    transliteration = transliteration,
    tags = Json.encodeToString(tags)
)
