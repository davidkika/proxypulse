package com.proxypulse.app.data.remote

import com.proxypulse.app.data.model.Proxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Fetches raw MTProto proxy lists from public sources and normalises them
 * into [Proxy] objects (before any region filter or liveness check).
 *
 * Sources are intentionally easy to edit — add/remove URLs in [SOURCES].
 * The parser understands three shapes:
 *   1. JSON arrays of objects (mtpro.xyz style: host/port/secret/country)
 *   2. tg:// and https://t.me/proxy deep links
 *   3. plain "host:port:secret" lines
 */
class ProxySource(
    private val client: OkHttpClient = defaultClient()
) {

    companion object {
        /** Public MTProto sources. mtpro.xyz includes per-proxy country codes. */
        val SOURCES = listOf(
            "https://raw.githubusercontent.com/SoliSpirit/mtproto/master/all_proxies.txt",
            "https://raw.githubusercontent.com/kort0881/telegram-proxy-collector/main/proxy_all.txt",
            "https://raw.githubusercontent.com/kort0881/telegram-proxy-collector/main/proxy_ru.txt",
            "https://raw.githubusercontent.com/kort0881/telegram-proxy-collector/main/proxy_eu.txt",
            "https://raw.githubusercontent.com/kort0881/telegram-proxy-collector/main/verified/proxy_all_verified.json",
            "https://raw.githubusercontent.com/shablin/mtproto-proxy/main/data/valid_proxy.json",
            "https://raw.githubusercontent.com/shablin/mtproto-proxy/main/data/valid_proxy.txt",
            "https://raw.githubusercontent.com/ALIILAPRO/MTProtoProxy/main/mtproto.txt",
            "https://raw.githubusercontent.com/Argh94/Proxy-List/main/mtproto.txt",
            "https://raw.githubusercontent.com/Grim1313/mtproto-for-telegram/main/all_proxies.txt"
        )

        private fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /** Fetch every source, merge, and de-duplicate by id. Network errors per
     *  source are swallowed so one dead URL never breaks the whole refresh. */
    suspend fun fetchAll(): List<Proxy> = withContext(Dispatchers.IO) {
        SOURCES
            .flatMap { url -> runCatching { fetch(url) }.getOrDefault(emptyList()) }
            .distinctBy { it.id }
    }

    private fun fetch(url: String): List<Proxy> {
        val request = Request.Builder().url(url)
            .header("User-Agent", "ProxyPulse/1.0 (Android)")
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            val body = resp.body?.string()?.trim().orEmpty()
            if (body.isEmpty()) return emptyList()
            return parse(body)
        }
    }

    private fun parse(body: String): List<Proxy> = when (body.first()) {
        '[' -> parseJsonArray(JSONArray(body))
        '{' -> parseJsonObject(JSONObject(body))
        else -> parseLines(body)
    }

    private fun parseJsonObject(obj: JSONObject): List<Proxy> {
        // Some feeds wrap the array under a key like "proxies".
        for (key in listOf("proxies", "data", "result", "list")) {
            if (obj.has(key)) {
                val v = obj.get(key)
                if (v is JSONArray) return parseJsonArray(v)
            }
        }
        return emptyList()
    }

    private fun parseJsonArray(arr: JSONArray): List<Proxy> {
        val out = ArrayList<Proxy>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val host = o.firstString("host", "ip", "server", "addr", "address") ?: continue
            val port = o.firstInt("port") ?: continue
            val secret = o.firstString("secret", "secret_hex", "key") ?: continue
            val country = o.firstString("country", "cc", "country_code", "geo").orEmpty()
            if (host.isBlank() || secret.isBlank() || port !in 1..65535) continue
            out += Proxy(host.trim(), port, secret.trim(), country.uppercase().take(2))
        }
        return out
    }

    private fun parseLines(text: String): List<Proxy> {
        val out = ArrayList<Proxy>()
        text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            parseLink(line)?.let { out += it } ?: parseTriple(line)?.let { out += it }
        }
        return out
    }

    private fun parseLink(line: String): Proxy? {
        if (!line.contains("proxy?", ignoreCase = true)) return null
        val query = line.substringAfter('?', "")
        val params = query.split('&').mapNotNull {
            val k = it.substringBefore('=', "")
            val v = it.substringAfter('=', "")
            if (k.isEmpty()) null else k.lowercase() to v
        }.toMap()
        val host = params["server"] ?: return null
        val port = params["port"]?.toIntOrNull() ?: return null
        val secret = params["secret"] ?: return null
        return Proxy(host, port, secret, "")
    }

    private fun parseTriple(line: String): Proxy? {
        val parts = line.split(':', ';', ',', ' ').filter { it.isNotBlank() }
        if (parts.size < 3) return null
        val port = parts[1].toIntOrNull() ?: return null
        return Proxy(parts[0], port, parts[2], parts.getOrNull(3)?.uppercase()?.take(2).orEmpty())
    }
}

private fun JSONObject.firstString(vararg keys: String): String? {
    for (k in keys) if (has(k) && !isNull(k)) return optString(k).takeIf { it.isNotBlank() }
    return null
}

private fun JSONObject.firstInt(vararg keys: String): Int? {
    for (k in keys) if (has(k) && !isNull(k)) {
        optString(k).toIntOrNull()?.let { return it }
        runCatching { return getInt(k) }
    }
    return null
}
