package com.fabiofiorini.pathtracker.tracking

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.osmdroid.util.GeoPoint

class TrackingManager {

    companion object {
        val current: TrackingManager by lazy { TrackingManager() }
    }

    val points = mutableStateListOf<GeoPoint>()

    val timestamps = mutableStateListOf<Long>()

    var elapsedSeconds = mutableLongStateOf(0L)

    var distanceMeters = mutableFloatStateOf(0f)

    var isTracking = mutableStateOf(false)

    var steps = mutableIntStateOf(0)

    var locationEnabled = mutableStateOf(true)

    fun reset() {
        points.clear()
        timestamps.clear()
        elapsedSeconds.longValue = 0L
        distanceMeters.floatValue = 0f
        steps.intValue = 0
        isTracking.value = false
        locationEnabled.value = true
    }
}
