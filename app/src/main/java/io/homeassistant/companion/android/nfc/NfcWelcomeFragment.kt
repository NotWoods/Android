package io.homeassistant.companion.android.nfc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.composethemeadapter.MdcTheme
import io.homeassistant.companion.android.R

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class NfcWelcomeFragment : Fragment() {

    private val viewModel: NfcViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            setContent {
                MdcTheme {
                    NfcWelcomeView(
                        onReadClick = {
                            findNavController().navigate(R.id.action_NFC_READ)
                        },
                        onWriteClick = {
                            viewModel.postNewUUID()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nfcReadObserver = Observer<String> {
            findNavController().navigate(R.id.action_NFC_READ)
        }
        viewModel.nfcReadEvent.observe(viewLifecycleOwner, nfcReadObserver)

        val nfcWriteTagObserver = Observer<String> {
            findNavController().navigate(R.id.action_NFC_WRITE)
        }
        viewModel.nfcWriteTagEvent.observe(viewLifecycleOwner, nfcWriteTagObserver)
    }
}
