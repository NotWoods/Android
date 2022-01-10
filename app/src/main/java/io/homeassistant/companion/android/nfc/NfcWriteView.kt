package io.homeassistant.companion.android.nfc

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.homeassistant.companion.android.common.R as commonR

@Composable
fun NfcWriteView(
    tagIdentifier: String
) {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(commonR.string.nfc_write_tag_instructions, tagIdentifier)
        )
    }
}

@Composable
@Preview
private fun PreviewNfcWrite() {
    NfcWriteView(
        tagIdentifier = "123456-789"
    )
}

@Composable
fun NfcWriteEntryView(
    viewModel: NfcViewModel,
    tagIdentifier: String
) {
    SideEffect {
        viewModel.nfcTagIdToWrite.value = tagIdentifier
    }

    NfcWriteView(tagIdentifier = tagIdentifier)
}
