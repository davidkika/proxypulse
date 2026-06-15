package com.proxypulse.app.data.check

import com.proxypulse.app.data.model.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Verifies proxies by opening a raw TCP connection to host:port and measuring
 * the round-trip latency. Dead / unreachable proxies are dropped — only the
 * survivors are returned, so every refresh "immediately removes" non-working
 * entries by construction.
 */
class ProxyChecker(
    private val timeoutMs: Int = 4000,
    private val maxParallel: Int = 40
) {

    /** Returns only the proxies that answered, each stamped with its ping. */
    suspend fun checkAll(proxies: List<Proxy>): List<Proxy> = coroutineScope {
        val gate = Semaphore(maxParallel)
        proxies
            .map { proxy ->
                async { gate.withPermit { check(proxy) } }
            }
            .awaitAll()
            .filterNotNull()
    }

    /** One liveness probe. Returns the proxy with a ping, or null if dead. */
    suspend fun check(proxy: Proxy): Proxy? = withContext(Dispatchers.IO) {
        val socket = Socket()
        val start = System.nanoTime()
        try {
            socket.connect(InetSocketAddress(proxy.host, proxy.port), timeoutMs)
            val pingMs = ((System.nanoTime() - start) / 1_000_000).toInt()
            proxy.copy(pingMs = pingMs, lastChecked = System.currentTimeMillis())
        } catch (_: Exception) {
            null
        } finally {
            runCatching { socket.close() }
        }
    }
}
