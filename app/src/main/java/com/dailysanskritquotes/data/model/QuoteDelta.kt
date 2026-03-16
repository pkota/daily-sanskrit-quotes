package com.dailysanskritquotes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class QuoteDelta(
    val fromVersion: Int,
    val toVersion: Int,
    val changes: List<DeltaChange>
)
