package io.homeassistant.companion.android.nfc

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.homeassistant.companion.android.common.data.integration.IntegrationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.homeassistant.companion.android.common.R as commonR

@Composable
fun NfcEditView(
    etTagIdentifier: String,
    deviceId: String,
    onDuplicate: (etTagIdentifier: String) -> Unit,
    onFire: (etTagIdentifier: String) -> Unit,
    onShare: (etTagExampleTrigger: String) -> Unit
) {
    val etTagExampleTrigger =
        "- platform: event\n  event_type: tag_scanned\n  event_data:\n    device_id: $deviceId\n    tag_id: $etTagIdentifier"

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        TextField(
            label = { Text(text = stringResource(commonR.string.nfc_tag_identifier)) },
            value = etTagIdentifier,
            readOnly = true,
            onValueChange = {}
        )

        Row {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                onClick = { onDuplicate(etTagIdentifier) }
            ) {
                Text(text = stringResource(commonR.string.nfc_btn_create_duplicate))
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                onClick = { onFire(etTagIdentifier) }
            ) {
                Text(text = stringResource(commonR.string.nfc_btn_fire_event))
            }
        }

        TextField(
            modifier = Modifier.padding(top = 48.dp),
            label = { Text(text = stringResource(commonR.string.nfc_example_trigger)) },
            value = etTagIdentifier,
            readOnly = true,
            onValueChange = {}
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onShare(etTagExampleTrigger) }
        ) {
            Text(text = stringResource(commonR.string.nfc_btn_share))
        }
    }
}

@Composable
@Preview
private fun PreviewNfcEdit() {
    NfcEditView(
        etTagIdentifier = "",
        deviceId = "",
        onDuplicate = {},
        onFire = {},
        onShare = {}
    )
}

private const val TAG = "NfcEdit"

@SuppressLint("HardwareIds")
@Composable
fun NfcEditEntryView(
    etTagIdentifier: State<String>,
    mainScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    integrationUseCase: IntegrationRepository,
    onDuplicate: (etTagIdentifier: String) -> Unit,
) {
    val context = LocalContext.current
    val deviceId = remember(context.contentResolver) {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    NfcEditView(
        etTagIdentifier = etTagIdentifier.value,
        deviceId = deviceId,
        onDuplicate = onDuplicate,
        onFire = { uuid ->
            mainScope.launch {
                try {
                    integrationUseCase.scanTag(
                        hashMapOf("tag_id" to uuid)
                    )
                    snackbarHostState.showSnackbar(
                        message = context.getString(commonR.string.nfc_event_fired_success),
                        duration = SnackbarDuration.Short
                    )
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(
                        message = context.getString(commonR.string.nfc_event_fired_fail),
                        duration = SnackbarDuration.Long
                    )
                    Log.e(TAG, "Unable to send tag to Home Assistant.", e)
                }
            }
        },
        onShare = { etTagExampleTrigger ->
            val sendIntent: Intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, etTagExampleTrigger)
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            ContextCompat.startActivity(context, shareIntent, null)
        }
    )
}
