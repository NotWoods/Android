package io.homeassistant.companion.android.common.data.integration

/**
 * https://www.home-assistant.io/integrations/zone/
 */
data class ZoneAttributes(
    val hidden: Boolean,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val friendlyName: String,
    val icon: String?
)
