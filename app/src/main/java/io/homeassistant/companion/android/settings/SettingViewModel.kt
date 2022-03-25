package io.homeassistant.companion.android.settings

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.homeassistant.companion.android.BuildConfig
import io.homeassistant.companion.android.database.AppDatabase
import io.homeassistant.companion.android.database.settings.SensorUpdateFrequencySetting
import io.homeassistant.companion.android.database.settings.Setting
import io.homeassistant.companion.android.database.settings.WebsocketSetting
import io.homeassistant.companion.android.websocket.WebsocketManager
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(
    application
) {
    private val settingsDao = AppDatabase.getInstance(application).settingsDao()

    private val defaultSetting = Setting(
        id = 0,
        websocketSetting = if (BuildConfig.FLAVOR == "full") WebsocketSetting.NEVER else WebsocketSetting.ALWAYS,
        sensorUpdateFrequency = SensorUpdateFrequencySetting.NORMAL
    )

    fun updateWebsocketSetting(currentSetting: Setting, setting: WebsocketSetting) {
        viewModelScope.launch {
            settingsDao.insert(currentSetting.copy(websocketSetting = setting))
            WebsocketManager.start(getApplication())
        }
    }

    fun updateSensorSetting(currentSetting: Setting, setting: SensorUpdateFrequencySetting) {
        viewModelScope.launch {
            settingsDao.insert(currentSetting.copy(sensorUpdateFrequency = setting))
        }
    }

    // Once we support more than one instance we can get the setting per instance
    @Composable
    fun getSettingState(id: Int): State<Setting> {
        val flow = settingsDao.getFlow(id)
        val initial = defaultSetting.copy(id = id)
        return flow.collectAsState(initial = initial)
    }
}
