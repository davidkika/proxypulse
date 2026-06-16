package com.proxypulse.app.data.repository

import android.content.Context
import com.proxypulse.app.data.check.ProxyChecker
import com.proxypulse.app.data.model.Proxy
import com.proxypulse.app.data.region.Regions
import com.proxypulse.app.data.remote.ProxySource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

data class RefreshStatus(
    val isRefreshing: Boolean = false,
    val lastUpdated: Long = 0L,
    val fetched: Int = 0,
    val regional: Int = 0,
    val alive: Int = 0,
    val error: String? = null
)

class ProxyRepository(
    context: Context,
    private val source: ProxySource = ProxySource(),
    private val checker: ProxyChecker = ProxyChecker()
) {
    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, "proxies.json")
    private val json = Json { ignoreUnknownKeys = true }
    private val refreshMutex = Mutex()

    private val _proxies = MutableStateFlow(loadFromDisk())
    val proxies: StateFlow<List<Proxy>> = _proxies.asStateFlow()

    private val _status = MutableStateFlow(
        RefreshStatus(lastUpdated = loadTimestamp(), alive = _proxies.value.size)
    )
    val status: StateFlow<RefreshStatus> = _status.asStateFlow()

    fun isStale(): Boolean {
        val ts = _status.value.lastUpdated
        return _proxies.value.isEmpty() || (System.currentTimeMillis() - ts) > MAX_AGE_MS
    }

    suspend fun refresh() {
        if (!refreshMutex.tryLock()) return
        try {
            _status.update { it.copy(isRefreshing = true, error = null) }

            val fetched = source.fetchAll()
            val regional = fetched.filter { Regions.isAllowed(it.country) }
            val checked = checker.checkAll(regional).sortedBy { it.pingMs }

            // Fallback: if the liveness probe killed everything (DPI/VPN often
            // blocks our raw TCP test even when the proxy itself works in
            // Telegram), show the fetched list unchecked instead of nothing.
            val result = if (checked.isEmpty() && regional.isNotEmpty()) regional else checked

            // Diagnostic note so the cause is visible in the UI.
            val note = when {
                fetched.isEmpty() ->
                    "Источники недоступны — похоже, заблокирован GitHub или нет сети (попробуйте без VPN)"
                checked.isEmpty() ->
                    "Скачано ${regional.size}, но проверка живости не прошла — показаны без проверки (часто из-за VPN/DPI)"
                else -> null
            }

            _proxies.value = result
            saveToDisk(result)

            _status.update {
                it.copy(
                    isRefreshing = false,
                    lastUpdated = System.currentTimeMillis(),
                    fetched = fetched.size,
                    regional = regional.size,
                    alive = result.size,
                    error = note
                )
            }
        } catch (e: Exception) {
            _status.update { it.copy(isRefreshing = false, error = e.message ?: "Ошибка обновления") }
        } finally {
            refreshMutex.unlock()
        }
    }

    private fun loadFromDisk(): List<Proxy> = runCatching {
        if (!file.exists()) emptyList()
        else json.decodeFromString<Stored>(file.readText()).proxies
    }.getOrDefault(emptyList())

    private fun loadTimestamp(): Long = runCatching {
        if (!file.exists()) 0L else json.decodeFromString<Stored>(file.readText()).updatedAt
    }.getOrDefault(0L)

    private fun saveToDisk(list: List<Proxy>) = runCatching {
        file.writeText(json.encodeToString(Stored(System.currentTimeMillis(), list)))
    }

    @kotlinx.serialization.Serializable
    private data class Stored(val updatedAt: Long, val proxies: List<Proxy>)

    companion object {
        const val MAX_AGE_MS = 60 * 60 * 1000L
    }
}    context: Context,
    private val source: ProxySource = ProxySource(),
    private val checker: ProxyChecker = ProxyChecker()
) {
    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, "proxies.json")
    private val json = Json { ignoreUnknownKeys = true }
    private val refreshMutex = Mutex()

    private val _proxies = MutableStateFlow(loadFromDisk())
    val proxies: StateFlow<List<Proxy>> = _proxies.asStateFlow()

    private val _status = MutableStateFlow(
        RefreshStatus(lastUpdated = loadTimestamp(), alive = _proxies.value.size)
    )
    val status: StateFlow<RefreshStatus> = _status.asStateFlow()

    /** True if the stored list is empty or older than [MAX_AGE_MS]. */
    fun isStale(): Boolean {
        val ts = _status.value.lastUpdated
        return _proxies.value.isEmpty() || (System.currentTimeMillis() - ts) > MAX_AGE_MS
    }

    suspend fun refresh() {
        // Guard against overlapping refreshes (UI tap + worker at once).
        if (!refreshMutex.tryLock()) return
        try {
            _status.update { it.copy(isRefreshing = true, error = null) }

            val fetched = source.fetchAll()
            val regional = fetched.filter { Regions.isAllowed(it.country) }
            val alive = checker.checkAll(regional).sortedBy { it.pingMs }

            _proxies.value = alive
            saveToDisk(alive)

            _status.update {
                it.copy(
                    isRefreshing = false,
                    lastUpdated = System.currentTimeMillis(),
                    fetched = fetched.size,
                    regional = regional.size,
                    alive = alive.size,
                    error = null
                )
            }
        } catch (e: Exception) {
            _status.update { it.copy(isRefreshing = false, error = e.message ?: "Ошибка обновления") }
        } finally {
            refreshMutex.unlock()
        }
    }

    // ---- persistence -------------------------------------------------------

    private fun loadFromDisk(): List<Proxy> = runCatching {
        if (!file.exists()) emptyList()
        else json.decodeFromString<Stored>(file.readText()).proxies
    }.getOrDefault(emptyList())

    private fun loadTimestamp(): Long = runCatching {
        if (!file.exists()) 0L else json.decodeFromString<Stored>(file.readText()).updatedAt
    }.getOrDefault(0L)

    private fun saveToDisk(list: List<Proxy>) = runCatching {
        file.writeText(json.encodeToString(Stored(System.currentTimeMillis(), list)))
    }

    @kotlinx.serialization.Serializable
    private data class Stored(val updatedAt: Long, val proxies: List<Proxy>)

    companion object {
        const val MAX_AGE_MS = 60 * 60 * 1000L // 1 hour
    }
}
