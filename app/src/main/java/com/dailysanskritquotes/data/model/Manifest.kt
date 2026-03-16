package com.dailysanskritquotes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Manifest(
    val version: Int,
    val minDeltaVersion: Int,
    val totalQuotes: Int,
    val checksum: String,
    val updatedAt: String
)
