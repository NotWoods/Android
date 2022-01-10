package io.homeassistant.companion.android.nfc

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.homeassistant.companion.android.common.data.integration.IntegrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NfcUiEvent {
    data class TagRead(val tagIdentifier: String) : NfcUiEvent()
    data class TagWritten(val tagIdentifier: String) : NfcUiEvent()
}

private const val TAG = "NfcViewModel"

@HiltViewModel
class NfcViewModel @Inject constructor(
    private val integrationUseCase: IntegrationRepository
) : ViewModel() {

    val nfcTagIdToWrite = mutableStateOf<String?>(null)
    var simpleWrite = false
    var messageId: Int = -1

    private val _nfcEvents = MutableStateFlow<NfcUiEvent?>(null)
    val nfcEvents = _nfcEvents.asStateFlow()

    init {
        Log.i(TAG, "NfcViewModel created!")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "NfcViewModel destroyed!")
    }

    suspend fun scanTag(uuid: String) {
        integrationUseCase.scanTag(hashMapOf("tag_id" to uuid))
    }

    fun tagRead(tagIdentifier: String) = viewModelScope.launch {
        _nfcEvents.emit(NfcUiEvent.TagRead(tagIdentifier))
    }

    fun tagWritten(tagIdentifier: String) = viewModelScope.launch {
        _nfcEvents.emit(NfcUiEvent.TagWritten(tagIdentifier))
    }
}
