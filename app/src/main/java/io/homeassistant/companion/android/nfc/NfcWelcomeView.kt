package io.homeassistant.companion.android.nfc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.homeassistant.companion.android.common.R as commonR

@Composable
fun NfcWelcomeView(
    onReadClick: () -> Unit,
    onWriteClick: () -> Unit
) {
    Column {
        Text(
            text = stringResource(commonR.string.nfc_welcome_message),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                onClick = onReadClick
            ) {
                Text(text = stringResource(commonR.string.nfc_btn_read_tag))
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                onClick = onWriteClick
            ) {
                Text(text = stringResource(commonR.string.nfc_btn_write_tag))
            }
        }
    }
}

@Composable
@Preview
private fun PreviewNfcWelcome() {
    NfcWelcomeView(
        onReadClick = {},
        onWriteClick = {}
    )
}
