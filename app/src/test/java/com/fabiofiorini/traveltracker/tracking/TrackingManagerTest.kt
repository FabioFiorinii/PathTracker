package com.fabiofiorini.traveltracker.tracking

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.osmdroid.util.GeoPoint

class TrackingManagerTest {

    private val manager = TrackingManager()

    @Before
    fun setUp() {
        TrackingManager.current = manager
    }

    @After
    fun tearDown() {
        manager.reset()
        TrackingManager.current = null
    }

    @Test
    fun `initial state is all zeros`() {
        assertTrue(manager.points.isEmpty())
        assertEquals(0L, manager.elapsedSeconds.longValue)
        assertEquals(0f, manager.distanceMeters.floatValue)
        assertFalse(manager.isTracking.value)
    }

    @Test
    fun `reset clears all state`() {
        manager.points.add(GeoPoint(45.0, 9.0))
        manager.elapsedSeconds.longValue = 120L
        manager.distanceMeters.floatValue = 500f
        manager.isTracking.value = true

        manager.reset()

        assertTrue(manager.points.isEmpty())
        assertEquals(0L, manager.elapsedSeconds.longValue)
        assertEquals(0f, manager.distanceMeters.floatValue)
        assertFalse(manager.isTracking.value)
    }
}
