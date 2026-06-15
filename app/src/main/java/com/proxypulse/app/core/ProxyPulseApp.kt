package com.proxypulse.app.core

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.proxypulse.app.work.RefreshWorker
import java.util.concurrent.TimeUnit

class ProxyPulseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.repository(this) // warm up + load cache from disk
        scheduleHourlyRefresh()
    }

    private fun scheduleHourlyRefresh() {
        val request = PeriodicWorkRequestBuilder<RefreshWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RefreshWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
