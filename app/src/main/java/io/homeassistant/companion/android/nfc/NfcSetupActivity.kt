package io.homeassistant.companion.android.nfc

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import io.homeassistant.companion.android.common.R

@AndroidEntryPoint
class NfcSetupActivity : ComponentActivity() {

    private val viewModel by viewModels<NfcViewModel>()
    private var nfcAdapter: NfcAdapter? = null

    companion object {
        val TAG = NfcSetupActivity::class.simpleName
        const val EXTRA_TAG_VALUE = "tag_value"
        const val EXTRA_MESSAGE_ID = "message_id"

        fun newInstance(context: Context, tagId: String? = null, messageId: Int = -1): Intent {
            return Intent(context, NfcSetupActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE_ID, messageId)
                if (tagId != null) {
                    putExtra(EXTRA_TAG_VALUE, tagId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val tagValue = intent.getStringExtra(EXTRA_TAG_VALUE)
        viewModel.simpleWrite = tagValue != null
        if (tagValue != null) {
            viewModel.nfcTagIdToWrite.value = tagValue
        }
        viewModel.messageId = intent.getIntExtra(EXTRA_MESSAGE_ID, -1)

        setContent {
            MdcTheme {
                NfcSetupView(
                    navController = rememberNavController(),
                    viewModel = viewModel,
                    startDestination = when {
                        tagValue != null -> "write/$tagValue"
                        else -> "welcome"
                    },
                    onUpNavigation = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            val nfcTagToWriteUUID = viewModel.nfcTagIdToWrite.value

            // Create new nfc tag
            if (nfcTagToWriteUUID == null) {
                val nfcTagId = readNfcTag(intent)
                if (nfcTagId == null) {
                    Log.w(TAG, "Unable to read tag!")
                    Toast.makeText(
                        applicationContext,
                        R.string.nfc_invalid_tag,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                try {
                    val nfcTagUrl = writeNfcTag(intent, nfcTagToWriteUUID)

                    Log.d(TAG, "Wrote nfc tag with url: $nfcTagUrl")
                    Toast.makeText(
                        applicationContext,
                        R.string.nfc_write_tag_success,
                        Toast.LENGTH_LONG
                    ).show()

                    // If we are a simple write it means the frontend asked us to write.  This means
                    // we should return the user as fast as possible back to the UI to continue what
                    // they were doing!
                    if (viewModel.simpleWrite) {
                        setResult(viewModel.messageId)
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext,
                        R.string.nfc_write_tag_error,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "Unable to write tag.", e)
                }
            }
        }
    }

    /**
     * Reads the data provided in the NFC tag and update the view model.
     * @return Tag ID that was read. Null if the tag was not created by Home Assistant.
     */
    private fun readNfcTag(intent: Intent): String? {
        val url = NFCUtil.extractNFCTagUrl(intent)
        val nfcTagId = NFCUtil.splitNfcTagId(url)
        return nfcTagId?.also {
            viewModel.tagRead(it)
        }
    }

    /**
     * Writes given data to the NFC tag.
     * @return URL written to the tag.
     */
    private fun writeNfcTag(intent: Intent, nfcTagToWriteUUID: String): String {
        val nfcTagUrl = "https://www.home-assistant.io/tag/$nfcTagToWriteUUID"
        NFCUtil.createNFCMessage(nfcTagUrl, intent)

        viewModel.tagWritten(nfcTagToWriteUUID)
        return nfcTagUrl
    }
}
