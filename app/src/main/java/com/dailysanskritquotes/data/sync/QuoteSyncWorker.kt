package com.dailysanskritquotes.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class QuoteSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val quoteUpdater: QuoteUpdater
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return when (quoteUpdater.syncIfNeeded()) {
            is SyncResult.AlreadyUpToDate,
            is SyncResult.DeltaApplied,
            is SyncResult.FullRebuild -> Result.success()
            is SyncResult.Failed -> Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "quote_sync"

        fun schedule(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<QuoteSyncWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
