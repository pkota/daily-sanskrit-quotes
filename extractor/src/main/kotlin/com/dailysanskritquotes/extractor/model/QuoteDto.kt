package com.dailysanskritquotes.extractor.model

import kotlinx.serialization.Serializable

@Serializable
data class QuoteDto(
    val id: String,
    val sanskritText: String,
    val englishTranslation: String,
    val attribution: String,
    val transliteration: String = "",
    val tags: List<String> = emptyList()
)
