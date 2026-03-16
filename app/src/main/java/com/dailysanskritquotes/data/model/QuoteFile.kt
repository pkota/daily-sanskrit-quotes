package com.dailysanskritquotes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuoteFile(
    val version: Int,
    val quotes: List<QuoteDto>
)
