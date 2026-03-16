package com.dailysanskritquotes.data.sync

import com.dailysanskritquotes.data.QuoteRepository
import com.dailysanskritquotes.data.model.Manifest
import com.dailysanskritquotes.data.model.QuoteDelta
import com.dailysanskritquotes.data.model.QuoteFile
import com.dailysanskritquotes.data.parser.QuoteFileParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

sealed class SyncResult {
    object AlreadyUpToDate : SyncResult()
    data class DeltaApplied(val fromVersion: Int, val toVersion: Int, val quotesChanged: Int) : SyncResult()
    data class FullRebuild(val version: Int, val totalQuotes: Int) : SyncResult()
    data class Failed(val reason: String) : SyncResult()
}

class QuoteUpdater(
    private val repository: QuoteRepository,
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    suspend fun syncIfNeeded(): SyncResult {
        if (!shouldSync(repository.getLastSyncTimestamp(), clock())) {
            return SyncResult.AlreadyUpToDate
        }

        return try {
            performSync()
        } catch (e: Exception) {
            SyncResult.Failed(e.message ?: "Unknown error")
        }
    }

    private suspend fun performSync(): SyncResult {
        // Step 1: Fetch manifest
        val manifestJson = try {
            httpClient.get("$baseUrl/manifest.json").bodyAsText()
        } catch (e: Exception) {
            return SyncResult.Failed("Failed to fetch manifest: ${e.message}")
        }

        val manifest = QuoteFileParser.parseManifest(manifestJson).getOrElse {
            return SyncResult.Failed("Failed to parse manifest: ${it.message}")
        }

        val localVersion = repository.getLocalVersion()

        // Step 2: Check if already up to date
        if (localVersion == manifest.version) {
            repository.setLastSyncTimestamp(clock())
            return SyncResult.AlreadyUpToDate
        }

        // Step 3: Decide delta vs full rebuild
        return if (localVersion >= manifest.minDeltaVersion) {
            applyDeltaSync(localVersion, manifest)
        } else {
            applyFullRebuild(manifest)
        }
    }

    private suspend fun applyDeltaSync(localVersion: Int, manifest: Manifest): SyncResult {
        val deltaJson = try {
            httpClient.get("$baseUrl/deltas/delta-$localVersion-${manifest.version}.json").bodyAsText()
        } catch (e: Exception) {
            return SyncResult.Failed("Failed to fetch delta: ${e.message}")
        }

        val delta = QuoteFileParser.parseDelta(deltaJson).getOrElse {
            return SyncResult.Failed("Failed to parse delta: ${it.message}")
        }

        repository.applyDelta(delta)
        repository.setLocalVersion(manifest.version)
        repository.setLastSyncTimestamp(clock())

        return SyncResult.DeltaApplied(
            fromVersion = localVersion,
            toVersion = manifest.version,
            quotesChanged = delta.changes.size
        )
    }

    private suspend fun applyFullRebuild(manifest: Manifest): SyncResult {
        val fullJson = try {
            httpClient.get("$baseUrl/quotes-full.json").bodyAsText()
        } catch (e: Exception) {
            return SyncResult.Failed("Failed to fetch full quotes: ${e.message}")
        }

        val quoteFile = QuoteFileParser.parseQuoteFile(fullJson).getOrElse {
            return SyncResult.Failed("Failed to parse full quotes: ${it.message}")
        }

        repository.rebuildFromFull(quoteFile.quotes)
        repository.setLocalVersion(manifest.version)
        repository.setLastSyncTimestamp(clock())

        return SyncResult.FullRebuild(
            version = manifest.version,
            totalQuotes = quoteFile.quotes.size
        )
    }

    companion object {
        private const val TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L

        /**
         * Pure function: determines whether a sync should be attempted based on timing.
         * Extracted for testability in property tests.
         */
        fun shouldSync(lastSyncTimestamp: Long, currentTime: Long): Boolean {
            return (currentTime - lastSyncTimestamp) >= TWENTY_FOUR_HOURS_MS
        }
    }
}
