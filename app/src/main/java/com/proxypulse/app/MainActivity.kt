package com.proxypulse.app

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proxypulse.app.data.model.Proxy
import com.proxypulse.app.ui.MainViewModel
import com.proxypulse.app.ui.screen.HomeScreen
import com.proxypulse.app.ui.theme.ProxyPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ProxyPulseTheme {
                val vm: MainViewModel = viewModel()
                HomeScreen(
                    viewModel = vm,
                    onAddToTelegram = { openInTelegram(it) },
                    onCopy = { copyLink(it) }
                )
            }
        }
    }

    /** Opens Telegram and shows the "Enable this proxy?" dialog. */
    private fun openInTelegram(proxy: Proxy) {
        val candidates = listOf(proxy.tgLink(), proxy.httpsLink())
        for (link in candidates) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
                // try next candidate
            }
        }
        Toast.makeText(this, "Не удалось открыть Telegram", Toast.LENGTH_SHORT).show()
    }

    private fun copyLink(proxy: Proxy) {
        val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cb.setPrimaryClip(ClipData.newPlainText("MTProto proxy", proxy.tgLink()))
        Toast.makeText(this, "Ссылка на прокси скопирована", Toast.LENGTH_SHORT).show()
    }
}
