package com.dailysanskritquotes.extractor.model

import kotlinx.serialization.Serializable

@Serializable
data class QuoteFile(
    val version: Int,
    val quotes: List<QuoteDto>
)
