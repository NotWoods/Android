package io.homeassistant.companion.android.util

import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URL

object UrlHandler {
    fun handle(base: URL?, input: String): URL? {
        return when {
            isAbsoluteUrl(input) -> {
                URL(input)
            }
            input.startsWith("homeassistant://navigate/") -> {
                (base.toString() + input.removePrefix("homeassistant://navigate/")).toHttpUrlOrNull()?.toUrl()
            }
            else -> {
                (base.toString() + input.removePrefix("/")).toHttpUrlOrNull()?.toUrl()
            }
        }
    }

    fun isAbsoluteUrl(it: String?): Boolean {
        return Regex("^https?://").containsMatchIn(it.toString())
    }

    fun splitNfcTagId(url: Uri?): String? {
        val matches =
            Regex("^https?://www\\.home-assistant\\.io/tag/(.*)").find(
                url.toString()
            )
        return matches?.groups?.get(1)?.value
    }

    fun extractNfcTagId(intent: Intent): String? {
        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED) {
            return null
        }

        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        val ndefMessage = rawMessages?.get(0) as NdefMessage?
        val url = ndefMessage?.records?.get(0)?.toUri()
        return splitNfcTagId(url)
    }
}
