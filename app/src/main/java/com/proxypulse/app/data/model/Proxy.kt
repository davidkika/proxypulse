package com.proxypulse.app.data.model

import kotlinx.serialization.Serializable

/**
 * One MTProto proxy entry.
 *
 * [pingMs] is the measured TCP latency in milliseconds (-1 = not checked yet).
 * Only proxies that passed the liveness check are ever stored, so a value here
 * always means "alive at last check".
 */
@Serializable
data class Proxy(
    val host: String,
    val port: Int,
    val secret: String,
    val country: String,      // ISO 3166-1 alpha-2, uppercase (e.g. "DE", "US")
    val pingMs: Int = -1,
    val lastChecked: Long = 0L
) {
    /** Stable identifier used for de-duplication. */
    val id: String get() = "$host:$port:$secret"

    /** Deep link that opens Telegram and offers to enable this proxy. */
    fun tgLink(): String =
        "tg://proxy?server=$host&port=$port&secret=$secret"

    /** Web fallback that also resolves into the Telegram "Enable proxy?" dialog. */
    fun httpsLink(): String =
        "https://t.me/proxy?server=$host&port=$port&secret=$secret"
}
