package com.proxypulse.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.proxypulse.app.core.ServiceLocator
import com.proxypulse.app.data.model.Proxy
import com.proxypulse.app.data.region.Region
import com.proxypulse.app.data.region.Regions
import com.proxypulse.app.data.repository.ProxyRepository
import com.proxypulse.app.data.repository.RefreshStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class HomeUiState(
    val proxies: List<Proxy> = emptyList(),
    val status: RefreshStatus = RefreshStatus(),
    val region: Region = Region.ALL,
    val nextRefreshInSec: Long = 0L,
    val counts: Map<Region, Int> = emptyMap()
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: ProxyRepository = ServiceLocator.repository(app)

    private val _region = MutableStateFlow(Region.ALL)
    val region: StateFlow<Region> = _region.asStateFlow()

    /** Emits the current epoch millis every second to drive the countdown. */
    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(repo.proxies, repo.status, _region, ticker) { proxies, status, region, now ->
            val filtered = proxies.filter { Regions.matches(region, it.country) }
            val counts = mapOf(
                Region.ALL to proxies.size,
                Region.EUROPE to proxies.count { Regions.matches(Region.EUROPE, it.country) },
                Region.AMERICA to proxies.count { Regions.matches(Region.AMERICA, it.country) }
            )
            val next = if (status.lastUpdated == 0L) 0L
            else (status.lastUpdated + ProxyRepository.MAX_AGE_MS - now) / 1000
            HomeUiState(
                proxies = filtered,
                status = status,
                region = region,
                nextRefreshInSec = next.coerceAtLeast(0),
                counts = counts
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        // Auto-refresh on launch when cache is empty or older than an hour.
        if (repo.isStale()) refresh()
    }

    fun setRegion(region: Region) { _region.value = region }

    fun refresh() {
        viewModelScope.launch { repo.refresh() }
    }
}
