package io.homeassistant.companion.android.common.data.integration

import io.homeassistant.companion.android.common.data.integration.impl.entities.RateLimitResponse
import io.homeassistant.companion.android.common.data.websocket.impl.entities.GetConfigResponse
import kotlinx.coroutines.flow.Flow

interface IntegrationRepository {

    /**
     * https://developers.home-assistant.io/docs/api/native-app-integration/setup#registering-the-device-1
     */
    suspend fun registerDevice(deviceRegistration: DeviceRegistration)
    suspend fun updateRegistration(deviceRegistration: DeviceRegistration)
    suspend fun getRegistration(): DeviceRegistration

    suspend fun isRegistered(): Boolean

    /**
     * https://developers.home-assistant.io/docs/api/native-app-integration/notifications/#rate-limits
     */
    suspend fun getNotificationRateLimits(): RateLimitResponse

    /**
     * Render a Home Assistant template.
     * https://developers.home-assistant.io/docs/api/native-app-integration/sending-data#render-templates
     * @return Rendered template in plain text.
     */
    suspend fun renderTemplate(template: String, variables: Map<String, String>): String

    /**
     * Inform Home Assistant of new location information for this device.
     * https://developers.home-assistant.io/docs/api/native-app-integration/sending-data#update-device-location
     */
    suspend fun updateLocation(updateLocation: UpdateLocation)

    /**
     * Get all enabled zones.
     * Zones represent certain geographic regions, such as the user's home area.
     */
    suspend fun getZones(): Array<Entity<ZoneAttributes>>

    /** Controls if the status bar and navigation bar are hidden */
    suspend fun setFullScreenEnabled(enabled: Boolean)
    suspend fun isFullScreenEnabled(): Boolean

    /** Controls if the screen will stay on forever while the app is open */
    suspend fun setKeepScreenOnEnabled(enabled: Boolean)
    suspend fun isKeepScreenOnEnabled(): Boolean

    /** Controls if the web UI can be zoomed in using pinch-to-zoom */
    suspend fun setPinchToZoomEnabled(enabled: Boolean)
    suspend fun isPinchToZoomEnabled(): Boolean

    /** Controls if videos in the web UI will start playing automatically */
    suspend fun setAutoPlayVideo(enabled: Boolean)
    suspend fun isAutoPlayVideoEnabled(): Boolean

    /** Controls if the WebView can be debugged by developers */
    suspend fun setWebViewDebugEnabled(enabled: Boolean)
    suspend fun isWebViewDebugEnabled(): Boolean

    /** Controls the relative time when the user session will time out (in seconds) */
    suspend fun sessionTimeOut(value: Int)
    suspend fun getSessionTimeOut(): Int

    /**
     * Controls the exact time when the user session will time out.
     * Not user configurable.
     * @see [sessionTimeOut]
     */
    suspend fun setSessionExpireMillis(value: Long)
    suspend fun getSessionExpireMillis(): Long

    suspend fun getTileShortcuts(): List<String>
    suspend fun setTileShortcuts(entities: List<String>)
    suspend fun getTemplateTileContent(): String
    suspend fun setTemplateTileContent(content: String)
    suspend fun getTemplateTileRefreshInterval(): Int
    suspend fun setTemplateTileRefreshInterval(interval: Int)
    suspend fun setWearHapticFeedback(enabled: Boolean)
    suspend fun getWearHapticFeedback(): Boolean
    suspend fun setWearToastConfirmation(enabled: Boolean)
    suspend fun getWearToastConfirmation(): Boolean
    suspend fun getShowShortcutText(): Boolean
    suspend fun setShowShortcutTextEnabled(enabled: Boolean)

    suspend fun getHomeAssistantVersion(): String
    suspend fun isHomeAssistantVersionAtLeast(year: Int, month: Int, release: Int): Boolean

    suspend fun getConfig(): GetConfigResponse
    suspend fun getServices(): List<Service>?

    suspend fun getEntities(): List<Entity<Any>>?
    suspend fun getEntity(entityId: String): Entity<Map<String, Any>>?
    suspend fun getEntityUpdates(): Flow<Entity<*>>?

    suspend fun callService(domain: String, service: String, serviceData: HashMap<String, Any>)

    suspend fun scanTag(data: HashMap<String, Any>)

    suspend fun fireEvent(eventType: String, eventData: Map<String, Any>)

    suspend fun registerSensor(sensorRegistration: SensorRegistration<Any>)
    suspend fun updateSensors(sensors: Array<SensorRegistration<Any>>): Boolean

    suspend fun shouldNotifySecurityWarning(): Boolean
}
