package io.homeassistant.companion.android.settings.shortcuts

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maltaisn.icondialog.pack.IconPack
import dagger.hilt.android.lifecycle.HiltViewModel
import io.homeassistant.companion.android.common.R
import io.homeassistant.companion.android.common.data.integration.Entity
import io.homeassistant.companion.android.common.data.integration.IntegrationRepository
import io.homeassistant.companion.android.common.icons.IconDialogCompat
import io.homeassistant.companion.android.webview.WebViewActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N_MR1)
@HiltViewModel
class ManageShortcutsViewModel @Inject constructor(
    private val integrationUseCase: IntegrationRepository,
    application: Application
) : AndroidViewModel(application) {

    lateinit var iconDialogCompat: IconDialogCompat

    val app = application
    private val TAG = "ShortcutViewModel"
    private lateinit var iconPack: IconPack
    private var shortcutManager = application.applicationContext.getSystemService<ShortcutManager>()!!
    val canPinShortcuts = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && shortcutManager.isRequestPinShortcutSupported
    var pinnedShortcuts: MutableList<ShortcutInfo> = shortcutManager.pinnedShortcuts
        private set
    var dynamicShortcuts: MutableList<ShortcutInfo> = shortcutManager.dynamicShortcuts
        private set

    var entities = mutableStateMapOf<String, Entity<*>>()
        private set

    data class Shortcut(
        var id: MutableState<String?>,
        var selectedIcon: MutableState<String>,
        var label: MutableState<String>,
        var desc: MutableState<String>,
        var path: MutableState<String>,
        var type: MutableState<String>,
        var drawable: MutableState<Drawable?>,
        var delete: MutableState<Boolean>
    )

    var shortcuts = mutableStateListOf<Shortcut>()
        private set

    init {
        viewModelScope.launch {
            integrationUseCase.getEntities()?.forEach {
                entities[it.entityId] = it
            }
        }
        Log.d(TAG, "We have ${dynamicShortcuts.size} dynamic shortcuts")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Can we pin shortcuts: ${shortcutManager.isRequestPinShortcutSupported}")
            Log.d(TAG, "We have ${pinnedShortcuts.size} pinned shortcuts")
        }

        for (i in 0..5) {
            shortcuts.add(
                Shortcut(
                    mutableStateOf(""),
                    mutableStateOf(""),
                    mutableStateOf(""),
                    mutableStateOf(""),
                    mutableStateOf(""),
                    mutableStateOf("lovelace"),
                    mutableStateOf(AppCompatResources.getDrawable(application, R.drawable.ic_stat_ic_notification_blue)),
                    mutableStateOf(false)
                )
            )
        }

        if (dynamicShortcuts.size > 0) {
            for (i in 0 until dynamicShortcuts.size)
                setDynamicShortcutData(dynamicShortcuts[i].id, i)
        }
    }

    fun createShortcut(shortcutId: String, shortcutLabel: String, shortcutDesc: String, shortcutPath: String, bitmap: Bitmap? = null, iconName: String) {
        Log.d(TAG, "Attempt to add shortcut $shortcutId")
        val intent = Intent(
            WebViewActivity.newInstance(getApplication(), shortcutPath).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
        intent.action = shortcutPath
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        intent.putExtra("iconName", iconName)

        val shortcut = ShortcutInfo.Builder(getApplication(), shortcutId)
            .setShortLabel(shortcutLabel)
            .setLongLabel(shortcutDesc)
            .setIcon(
                if (bitmap != null)
                    Icon.createWithBitmap(bitmap)
                else
                    Icon.createWithResource(getApplication(), R.drawable.ic_stat_ic_notification_blue)
            )
            .setIntent(intent)
            .build()

        if (shortcutId.startsWith("shortcut")) {
            shortcutManager.addDynamicShortcuts(listOf(shortcut))
            dynamicShortcuts = shortcutManager.dynamicShortcuts
        } else {
            var isNewPinned = true
            for (item in pinnedShortcuts) {
                if (item.id == shortcutId) {
                    isNewPinned = false
                    Log.d(TAG, "Updating pinned shortcut: $shortcutId")
                    shortcutManager.updateShortcuts(listOf(shortcut))
                    Toast.makeText(getApplication(), R.string.shortcut_updated, Toast.LENGTH_SHORT).show()
                }
            }

            if (isNewPinned) {
                Log.d(TAG, "Requesting to pin shortcut: $shortcutId")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    shortcutManager.requestPinShortcut(shortcut, null)
                }
            }
        }
    }

    fun deleteShortcut(shortcutId: String) {
        shortcutManager.removeDynamicShortcuts(listOf(shortcutId))
        dynamicShortcuts = shortcutManager.dynamicShortcuts
    }

    fun setPinnedShortcutData(shortcutId: String) {
        viewModelScope.launch {
            for (item in pinnedShortcuts) {
                if (item.id == shortcutId) {
                    shortcuts.last().id.value = item.id
                    shortcuts.last().label.value = item.shortLabel.toString()
                    shortcuts.last().desc.value = item.longLabel.toString()
                    shortcuts.last().path.value = item.intent?.action.toString()
                    shortcuts.last().setIconFromIntent(item.intent)
                    if (shortcuts.last().path.value.startsWith("entityId:"))
                        shortcuts.last().type.value = "entityId"
                    else
                        shortcuts.last().type.value = "lovelace"
                }
            }
        }
    }

    fun setDynamicShortcutData(shortcutId: String, index: Int) {
        viewModelScope.launch {
            if (dynamicShortcuts.isNotEmpty()) {
                for (item in dynamicShortcuts) {
                    if (item.id == shortcutId) {
                        Log.d(TAG, "setting ${item.id} data")
                        shortcuts[index].label.value = item.shortLabel.toString()
                        shortcuts[index].desc.value = item.longLabel.toString()
                        shortcuts[index].path.value = item.intent?.action.toString()
                        shortcuts[index].setIconFromIntent(item.intent)
                        if (shortcuts[index].path.value.startsWith("entityId:"))
                            shortcuts[index].type.value = "entityId"
                        else
                            shortcuts[index].type.value = "lovelace"
                    }
                }
            }
        }
    }

    private suspend fun Shortcut.setIconFromIntent(intent: Intent?) {
        var iconName = intent?.getStringExtra("iconName")
        if (iconName.isNullOrBlank() && intent?.extras?.containsKey("iconId") == true) {
            val iconId = intent.extras?.getInt("iconId").toString().toIntOrNull()
            iconDialogCompat.initializeAsync()
            iconName = iconId?.let { iconDialogCompat.getIconName(iconId) }
        }

        selectedIcon.value = iconName.orEmpty()
        if (!iconName.isNullOrEmpty()) {
            iconDialogCompat.initializeAsync()
            val iconId = iconDialogCompat.getIconId(iconName)
            val iconDrawable = iconPack.icons[iconId]?.drawable
            drawable.value = iconDrawable?.let {
                val icon = DrawableCompat.wrap(iconDrawable)
                icon.setColorFilter(app.resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN)
                icon
            }
        }
    }

    fun updatePinnedShortcuts() {
        pinnedShortcuts = shortcutManager.pinnedShortcuts
    }
}
