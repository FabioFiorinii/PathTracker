package com.fabiofiorini.pathtracker.tracking

import org.junit.Test
import org.junit.Assert.*
import org.osmdroid.util.GeoPoint

class TrackingManagerTest {

    private val manager = TrackingManager()

    @Test
    fun `initial state is all zeros`() {
        assertTrue(manager.points.isEmpty())
        assertTrue(manager.timestamps.isEmpty())
        assertEquals(0L, manager.elapsedSeconds.longValue)
        assertEquals(0f, manager.distanceMeters.floatValue)
        assertFalse(manager.isTracking.value)
    }

    @Test
    fun `reset clears all state`() {
        manager.points.add(GeoPoint(45.0, 9.0))
        manager.timestamps.add(1000L)
        manager.elapsedSeconds.longValue = 120L
        manager.distanceMeters.floatValue = 500f
        manager.isTracking.value = true

        manager.reset()

        assertTrue(manager.points.isEmpty())
        assertTrue(manager.timestamps.isEmpty())
        assertEquals(0L, manager.elapsedSeconds.longValue)
        assertEquals(0f, manager.distanceMeters.floatValue)
        assertFalse(manager.isTracking.value)
    }

    @Test
    fun `current is a singleton`() {
        assertSame(TrackingManager.current, TrackingManager.current)
    }
}
