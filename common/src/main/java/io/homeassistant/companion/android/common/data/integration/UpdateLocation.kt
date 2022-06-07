package io.homeassistant.companion.android.common.data.integration

import androidx.annotation.IntRange
import androidx.annotation.Size

data class UpdateLocation(
    /** Current location as latitude and longitude. */
    @Size(2)
    val gps: Array<Double>,
    /** GPS accuracy in meters. */
    @IntRange(from=0)
    val gpsAccuracy: Int,
    /** Speed of the device in meters per second. */
    @IntRange(from=0)
    val speed: Int,
    /** Altitude of the device in meters. */
    @IntRange(from=0)
    val altitude: Int,
    /** The direction in which the device is traveling, measured in degrees and relative to due north. */
    @IntRange(from=0)
    val course: Int,
    /** The accuracy of the altitude value, measured in meters. */
    @IntRange(from=0)
    val verticalAccuracy: Int
)
