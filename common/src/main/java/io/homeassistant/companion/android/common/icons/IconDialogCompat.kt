package io.homeassistant.companion.android.common.icons

import android.content.res.AssetManager
import android.util.JsonReader
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import javax.inject.Inject

class IconDialogCompat @Inject constructor(
    private val assets: AssetManager
) {
    private var lazyMap: Map<Int, String>? = null
    private var loadingJob: Job? = null

    private val safeMap: Map<Int, String> get() {
        lazyMap?.let { return it }
        if (loadingJob != null) {
            throw UninitializedPropertyAccessException("Did not wait for initializeAsync to complete before using IconDialogCompat")
        } else {
            throw UninitializedPropertyAccessException("Did not call initializeSync or initializeAsync before using IconDialogCompat")
        }
    }

    /**
     * Read JSON map of icondialog IDs to material icon names.
     * @throws {IOException}
     */
    @WorkerThread
    private fun loadIconIdToNameMap(): Map<Int, String> {
        val inputStream = assets.open("icons/mdi_id_map.json")
        return JsonReader(InputStreamReader(inputStream)).use { reader ->
            val idToNameMap = mutableMapOf<Int, String>()
            reader.beginObject()
            while (reader.hasNext()) {
                val iconName = reader.nextName()
                val iconId = reader.nextInt()
                idToNameMap[iconId] = iconName
            }
            reader.endObject()

            idToNameMap
        }
    }

    /**
     * Initialize the map of icondialog IDs to material icon names.
     * Should only be run in a worker thread, such as in a Room migration.
     */
    @WorkerThread
    fun initializeSync() {
        if (lazyMap == null) {
            lazyMap = loadIconIdToNameMap()
        }
    }

    /**
     * Initialize the map of icondialog IDs to material icon names.
     */
    suspend fun initializeAsync() {
        if (lazyMap != null || loadingJob != null) {
            return
        }

        coroutineScope {
            loadingJob = launch(Dispatchers.IO) { initializeSync() }.also { it.join() }
        }
    }

    /**
     * Get the material icon name (ie "account-alert") that corresponds to the given icondialog ID.
     */
    fun getIconName(iconId: Int): String? = safeMap[iconId]

    fun getIconId(iconName: String): Int? {
        for ((id, name) in safeMap) {
            if (name == iconName) {
                return id
            }
        }
        return null
    }
}
