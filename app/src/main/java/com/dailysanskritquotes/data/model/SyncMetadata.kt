package com.dailysanskritquotes.data.model

data class SyncMetadata(
    val dbVersion: Int,
    val lastSyncTimestamp: Long,
    val baseUrl: String
)
