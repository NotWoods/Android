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
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class NfcWriteFragment : Fragment() {

    private val viewModel: NfcViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            setContent {
                val tagIdState = viewModel.nfcWriteTagEvent.observeAsState()
                MdcTheme {
                    NfcWriteView(
                        tagIdentifier = tagIdState.value
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nfcWriteTagDoneObserver = Observer<String> {
            findNavController().navigate(R.id.action_NFC_EDIT)
        }
        viewModel.nfcWriteTagDoneEvent.observe(viewLifecycleOwner, nfcWriteTagDoneObserver)
    }
}
