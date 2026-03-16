package com.dailysanskritquotes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeltaChange(
    val action: String,
    val quote: QuoteDto? = null,
    val quoteId: String? = null
)
