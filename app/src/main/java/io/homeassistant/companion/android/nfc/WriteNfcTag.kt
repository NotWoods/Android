package io.homeassistant.companion.android.nfc

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class WriteNfcTag : ActivityResultContract<WriteNfcTag.SimpleWriteMessage, Int>() {

    override fun createIntent(context: Context, input: SimpleWriteMessage): Intent {
        return NfcSetupActivity.newInstance(context).apply {
            putExtra(NfcSetupActivity.EXTRA_MESSAGE_ID, input.messageId)
            putExtra(NfcSetupActivity.EXTRA_TAG_VALUE, input.tagIdentifier)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Int {
        return resultCode
    }

    data class SimpleWriteMessage(val tagIdentifier: String?, val messageId: Int)
}