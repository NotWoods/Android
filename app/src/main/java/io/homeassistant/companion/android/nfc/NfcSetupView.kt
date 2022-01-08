package io.homeassistant.companion.android.nfc

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import io.homeassistant.companion.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class NfcScreen { Welcome, Write, Read, Edit }

@Composable
fun NfcSetupView(
    viewModel: NfcViewModel
) {
    val navController = rememberNavController()
    val etTagIdentifier = viewModel.nfcReadEvent.observeAsState()
    val scope = rememberCoroutineScope { Dispatchers.Main + Job() }

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NfcScreen.Welcome.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NfcScreen.Welcome.name) {
                NfcWelcomeView(
                    onReadClick = { navController.navigate(NfcScreen.Read.name) },
                    onWriteClick = { viewModel.postNewUUID() }
                )
            }
            composable(NfcScreen.Write.name) {
                NfcWriteView(
                    tagIdentifier = viewModel.nfcWriteTagEvent.observeAsState().value
                )
            }
            composable(NfcScreen.Read.name) {
                NfcReadView()
            }
            composable(NfcScreen.Edit.name) {
                NfcEditEntryView(
                    etTagIdentifier = etTagIdentifier,
                    mainScope = scope,
                    snackbarHostState = {},
                    onDuplicate = { uuid ->
                        viewModel.nfcWriteTagEvent.postValue(uuid)
                        navController.navigate(R.id.action_NFC_WRITE)
                    }
                )
            }
        }
    }
}