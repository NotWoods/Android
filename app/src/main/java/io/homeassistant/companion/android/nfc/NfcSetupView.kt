package io.homeassistant.companion.android.nfc

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.collectLatest
import java.util.*
import io.homeassistant.companion.android.common.R as commonR

@Composable
fun NfcSetupView(
    navController: NavHostController,
    viewModel: NfcViewModel,
    startDestination: String = "welcome",
    onUpNavigation: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.nfcEvents.flowWithLifecycle(lifecycleOwner.lifecycle).collectLatest { event ->
            when (event) {
                is NfcUiEvent.TagRead -> {
                    navController.navigate("edit/${event.tagIdentifier}")
                }
                is NfcUiEvent.TagWritten -> {
                    navController.navigate("edit/${event.tagIdentifier}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(commonR.string.nfc_title_nfc_setup))
                },
                navigationIcon = {
                    IconButton(onClick = onUpNavigation) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(commonR.string.up)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("welcome") {
                NfcWelcomeView(
                    onReadClick = { navController.navigate("read") },
                    onWriteClick = {
                        val uuid = UUID.randomUUID().toString()
                        navController.navigate("write/$uuid")
                    }
                )
            }
            composable(
                "write/{uuid}",
                arguments = listOf(navArgument("uuid") { type = NavType.StringType })
            ) { backStackEntry ->
                val tagIdentifier = backStackEntry.arguments?.getString("uuid")
                NfcWriteEntryView(
                    viewModel = viewModel,
                    tagIdentifier = tagIdentifier.orEmpty()
                )
            }
            composable("read") {
                NfcReadView()
            }
            composable(
                "edit/{uuid}",
                arguments = listOf(navArgument("uuid") { type = NavType.StringType })
            ) { backStackEntry ->
                val tagIdentifier = backStackEntry.arguments?.getString("uuid")
                NfcEditEntryView(
                    viewModel = viewModel,
                    tagIdentifier = tagIdentifier!!,
                    onDuplicate = {
                        viewModel.nfcTagIdToWrite.value = tagIdentifier
                        navController.navigate("write/$tagIdentifier")
                    }
                )
            }
        }
    }
}


