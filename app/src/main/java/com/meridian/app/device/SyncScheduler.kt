package com.meridian.app.device

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.glance.appwidget.updateAll
import com.meridian.app.device.widget.MeridianWidget
import com.meridian.app.di.container
import com.meridian.app.domain.repository.SyncResult
import com.meridian.app.domain.repository.SyncTrigger
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/** WorkManager sobrevive al cierre de la app y a reinicios: garantiza el envío de lo pendiente (RNF06). */
class SyncScheduler(private val context: Context) : SyncTrigger {
    private val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    override fun requestSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).setConstraints(constraints).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("$UNIQUE_NAME-periodic", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private companion object { const val UNIQUE_NAME = "meridian-sync" }
}

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val c = applicationContext.container
        val result = c.syncRepository.sync()
        runCatching { MeridianWidget().updateAll(applicationContext) }
        return when (result) {
            SyncResult.SUCCESS, SyncResult.NOT_SIGNED_IN, SyncResult.AUTH_EXPIRED -> Result.success()
            SyncResult.NETWORK_ERROR -> {
                if (runAttemptCount >= 3 &&
                    c.preferencesRepository.current().pendingSyncAlert &&
                    c.clockRepository.observePendingCount().first() > 0
                ) c.notifications.showPendingSyncAlert()
                Result.retry()
            }
        }
    }
}
