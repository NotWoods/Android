package io.homeassistant.companion.android.nfc

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import io.homeassistant.companion.android.common.R as commonR

@Preview
@Composable
fun NfcReadView() {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(commonR.string.nfc_read_tag_instructions)
        )
    }
}
