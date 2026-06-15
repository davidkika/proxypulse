package com.proxypulse.app.core

import android.content.Context
import com.proxypulse.app.data.repository.ProxyRepository

/**
 * Minimal service locator so the ViewModel and the background Worker share the
 * exact same [ProxyRepository] instance (and therefore the same state flows)
 * while running in the same process.
 */
object ServiceLocator {
    @Volatile
    private var repo: ProxyRepository? = null

    fun repository(context: Context): ProxyRepository =
        repo ?: synchronized(this) {
            repo ?: ProxyRepository(context.applicationContext).also { repo = it }
        }
}
