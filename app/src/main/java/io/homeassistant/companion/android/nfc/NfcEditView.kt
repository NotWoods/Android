package io.homeassistant.companion.android.nfc

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import io.homeassistant.companion.android.common.R as commonR

@Composable
fun NfcEditView(
    tagIdentifier: String,
    deviceId: String,
    onDuplicate: () -> Unit,
    onFire: () -> Unit,
    onShare: (shareIntent: Intent) -> Unit
) {
    val tagExampleTrigger = """
        - platform: event
          event_type: tag_scanned
          event_data:
            device_id: $deviceId
            tag_id: $tagIdentifier
    """.trimIndent()

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        TextField(
            label = { Text(text = stringResource(commonR.string.nfc_tag_identifier)) },
            value = tagIdentifier,
            readOnly = true,
            onValueChange = {}
        )

        Row {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                onClick = onDuplicate
            ) {
                Text(text = stringResource(commonR.string.nfc_btn_create_duplicate))
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                onClick = onFire
            ) {
                Text(text = stringResource(commonR.string.nfc_btn_fire_event))
            }
        }

        TextField(
            modifier = Modifier.padding(top = 48.dp),
            label = { Text(text = stringResource(commonR.string.nfc_example_trigger)) },
            value = tagExampleTrigger,
            readOnly = true,
            onValueChange = {}
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val sendIntent: Intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, tagExampleTrigger)
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                onShare(shareIntent)
            }
        ) {
            Text(text = stringResource(commonR.string.nfc_btn_share))
        }
    }
}

@Composable
@Preview
private fun PreviewNfcEdit() {
    NfcEditView(
        tagIdentifier = "123456-789",
        deviceId = "ABCD-EFG",
        onDuplicate = {},
        onFire = {},
        onShare = {}
    )
}

/**
 * Retrieves the hardware device ID using the current context.
 */
@SuppressLint("HardwareIds")
@Composable
fun androidId(): String {
    val contentResolver = LocalContext.current.contentResolver
    return remember(contentResolver) {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }
}

private const val TAG = "NfcEdit"

@Composable
fun NfcEditEntryView(
    viewModel: NfcViewModel,
    tagIdentifier: String,
    onDuplicate: () -> Unit,
) {
    val context = LocalContext.current
    val composableScope = rememberCoroutineScope()

    NfcEditView(
        tagIdentifier = tagIdentifier,
        deviceId = androidId(),
        onDuplicate = onDuplicate,
        onFire = {
            composableScope.launch {
                try {
                    viewModel.scanTag(tagIdentifier)
                    Toast.makeText(
                        context,
                        commonR.string.nfc_event_fired_success,
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        commonR.string.nfc_event_fired_fail,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "Unable to send tag to Home Assistant.", e)
                }
            }
        },
        onShare = { shareIntent ->
            ContextCompat.startActivity(context, shareIntent, null)
        }
    )
}
