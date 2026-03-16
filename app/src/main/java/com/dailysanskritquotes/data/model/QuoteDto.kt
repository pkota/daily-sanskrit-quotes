package com.dailysanskritquotes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuoteDto(
    val id: String,
    val sanskritText: String,
    val englishTranslation: String,
    val attribution: String
)
