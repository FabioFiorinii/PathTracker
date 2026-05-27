package com.fabiofiorini.traveltracker.tracking

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.osmdroid.util.GeoPoint

class TrackingManager {

    companion object {
        @Volatile
        var current: TrackingManager? = null
    }

    val points = mutableStateListOf<GeoPoint>()

    val timestamps = mutableStateListOf<Long>()

    var elapsedSeconds = mutableLongStateOf(0L)

    var distanceMeters = mutableFloatStateOf(0f)

    var isTracking = mutableStateOf(false)

    fun reset() {
        points.clear()
        timestamps.clear()
        elapsedSeconds.longValue = 0L
        distanceMeters.floatValue = 0f
        isTracking.value = false
    }
}
