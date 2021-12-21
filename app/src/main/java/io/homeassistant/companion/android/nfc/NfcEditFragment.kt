package io.homeassistant.companion.android.nfc

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import io.homeassistant.companion.android.R
import io.homeassistant.companion.android.common.data.integration.IntegrationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.homeassistant.companion.android.common.R as commonR

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class NfcEditFragment : Fragment() {

    val TAG = NfcEditFragment::class.simpleName

    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val viewModel: NfcViewModel by activityViewModels()

    @Inject
    lateinit var integrationUseCase: IntegrationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val deviceId = Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        return ComposeView(inflater.context).apply {
            setContent {
                val etTagIdentifier = viewModel.nfcReadEvent.observeAsState()
                MdcTheme {
                    NfcEditView(
                        etTagIdentifier = etTagIdentifier.value,
                        deviceId = deviceId,
                        onDuplicate = { uuid ->
                            viewModel.nfcWriteTagEvent.postValue(uuid)
                            findNavController().navigate(R.id.action_NFC_WRITE)
                        },
                        onFire = {
                            mainScope.launch {
                                val uuid: String = viewModel.nfcReadEvent.value.toString()
                                try {
                                    integrationUseCase.scanTag(
                                        hashMapOf("tag_id" to uuid)
                                    )
                                    Toast.makeText(activity, commonR.string.nfc_event_fired_success, Toast.LENGTH_SHORT)
                                        .show()
                                } catch (e: Exception) {
                                    Toast.makeText(activity, commonR.string.nfc_event_fired_fail, Toast.LENGTH_LONG)
                                        .show()
                                    Log.e(TAG, "Unable to send tag to Home Assistant.", e)
                                }
                            }
                        },
                        onShare = { etTagExampleTrigger ->
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, etTagExampleTrigger)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}
