package io.homeassistant.companion.android.nfc

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
