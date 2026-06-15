package com.proxypulse.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proxypulse.app.data.model.Proxy
import com.proxypulse.app.ui.MainViewModel
import com.proxypulse.app.ui.components.BrandHeader
import com.proxypulse.app.ui.components.FilterBar
import com.proxypulse.app.ui.components.ProxyCard
import com.proxypulse.app.ui.components.StatusHeader
import com.proxypulse.app.ui.theme.Brand
import com.proxypulse.app.ui.theme.Cyan
import com.proxypulse.app.ui.theme.TextMuted
import com.proxypulse.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAddToTelegram: (Proxy) -> Unit,
    onCopy: (Proxy) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .background(Brand.background())
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(14.dp))
            BrandHeader()
            Spacer(Modifier.height(18.dp))

            StatusHeader(
                liveCount = state.status.alive,
                lastUpdated = state.status.lastUpdated,
                nextRefreshInSec = state.nextRefreshInSec,
                isRefreshing = state.status.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))

            FilterBar(
                selected = state.region,
                counts = state.counts,
                onSelect = viewModel::setRegion,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))

            PullToRefreshBox(
                isRefreshing = state.status.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.status.isRefreshing && state.proxies.isEmpty() -> LoadingState()
                    state.proxies.isEmpty() -> EmptyState(state.status.error)
                    else -> ProxyList(state.proxies, onAddToTelegram, onCopy)
                }
            }
        }
    }
}

@Composable
private fun ProxyList(
    proxies: List<Proxy>,
    onAddToTelegram: (Proxy) -> Unit,
    onCopy: (Proxy) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        items(items = proxies, key = { it.id }) { proxy ->
            ProxyCard(
                proxy = proxy,
                onAddToTelegram = onAddToTelegram,
                onCopy = onCopy,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Cyan, strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text("Парсим и проверяем прокси…", style = TextStyle(color = TextSecondary, fontSize = 14.sp))
        }
    }
}

@Composable
private fun EmptyState(error: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📡", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                if (error != null) "Не удалось обновить" else "Пока нет живых прокси",
                style = TextStyle(color = TextSecondary, fontSize = 16.sp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                error ?: "Потяните вниз, чтобы обновить список",
                style = TextStyle(color = TextMuted, fontSize = 13.sp),
                textAlign = TextAlign.Center
            )
        }
    }
}
