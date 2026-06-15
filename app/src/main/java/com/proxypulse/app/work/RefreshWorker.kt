package com.proxypulse.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.proxypulse.app.core.ServiceLocator

/**
 * Runs once an hour (scheduled in [com.proxypulse.app.core.ProxyPulseApp]) to
 * re-fetch, re-check and replace the proxy list. Sharing the singleton
 * repository means an open UI updates instantly when this finishes.
 */
class RefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        ServiceLocator.repository(applicationContext).refresh()
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }

    companion object {
        const val UNIQUE_NAME = "proxypulse_hourly_refresh"
    }
}
