package com.fabiofiorini.traveltracker.tracking

import org.junit.Test
import org.junit.Assert.*
import org.osmdroid.util.GeoPoint

class TrackingManagerTest {

    @Test
    fun `initial state is all zeros`() {
        assertTrue(TrackingManager.points.isEmpty())
        assertEquals(0L, TrackingManager.elapsedSeconds.longValue)
        assertEquals(0f, TrackingManager.distanceMeters.floatValue)
        assertFalse(TrackingManager.isTracking.value)
    }

    @Test
    fun `reset clears all state`() {
        TrackingManager.points.add(GeoPoint(45.0, 9.0))
        TrackingManager.elapsedSeconds.longValue = 120L
        TrackingManager.distanceMeters.floatValue = 500f
        TrackingManager.isTracking.value = true

        TrackingManager.reset()

        assertTrue(TrackingManager.points.isEmpty())
        assertEquals(0L, TrackingManager.elapsedSeconds.longValue)
        assertEquals(0f, TrackingManager.distanceMeters.floatValue)
        assertFalse(TrackingManager.isTracking.value)
    }
}
