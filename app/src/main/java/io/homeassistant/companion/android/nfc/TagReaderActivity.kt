package io.homeassistant.companion.android.nfc

import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.homeassistant.companion.android.common.data.integration.IntegrationRepository
import io.homeassistant.companion.android.util.UrlHandler
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.homeassistant.companion.android.common.R as commonR

@AndroidEntryPoint
class TagReaderActivity : ComponentActivity() {

    val TAG = TagReaderActivity::class.simpleName

    @Inject
    lateinit var integrationUseCase: IntegrationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TagReaderView()
        }

        lifecycleScope.launch {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                val url = NFCUtil.extractNFCTagUrl(intent)
                try {
                    handleTag(url)
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext,
                        commonR.string.nfc_processing_tag_error,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "Unable to handle url (nfc): $url", e)
                    finish()
                }
            } else if (Intent.ACTION_VIEW == intent.action) {
                val url: Uri? = intent?.data
                try {
                    handleTag(url)
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext,
                        commonR.string.qrcode_processing_tag_error,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "Unable to handle url (qrcode): $url", e)
                    finish()
                }
            }
        }
    }

    private suspend fun handleTag(url: Uri?) {
        // https://www.home-assistant.io/tag/5f0ba733-172f-430d-a7f8-e4ad940c88d7

        val nfcTagId = UrlHandler.splitNfcTagId(url)
        Log.d(TAG, "nfcTagId: $nfcTagId")
        if (nfcTagId != null) {
            integrationUseCase.scanTag(hashMapOf("tag_id" to nfcTagId))
            Log.d(TAG, "Tag scanned to HA successfully")
        } else {
            Toast.makeText(
                applicationContext,
                commonR.string.nfc_processing_tag_error,
                Toast.LENGTH_LONG
            ).show()
        }
        finish()
    }
}
