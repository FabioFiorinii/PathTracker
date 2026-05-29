package com.fabiofiorini.pathtracker.util

import com.fabiofiorini.pathtracker.data.RoutePointEntity
import org.junit.Assert.*
import org.junit.Test

class RouteStatsCalculatorTest {

    @Test
    fun `calculateSpeeds returns empty for single point`() {
        val points = listOf(
            RoutePointEntity(routeId = 1, orderIndex = 0, lat = 45.0, lon = 9.0, timestampSec = 1)
        )
        val speeds = RouteStatsCalculator.calculateSpeeds(points)
        assertTrue(speeds.isEmpty())
    }

    @Test
    fun `calculateSpeeds returns known speed`() {
        val points = listOf(
            RoutePointEntity(routeId = 1, orderIndex = 0, lat = 45.0, lon = 9.0, timestampSec = 0),
            RoutePointEntity(routeId = 1, orderIndex = 1, lat = 45.00899, lon = 9.0, timestampSec = 3600)
        )
        val speeds = RouteStatsCalculator.calculateSpeeds(points)
        assertEquals(1, speeds.size)
        assertEquals(1.0f, speeds[0], 0.1f)
    }

    @Test
    fun `speedColor green for ratio 0`() {
        val color = RouteStatsCalculator.speedColor(0f)
        val green = (color shr 8) and 0xFF
        val red = (color shr 16) and 0xFF
        assertEquals(0, red)
        assertEquals(255, green)
    }

    @Test
    fun `speedColor red for ratio 1`() {
        val color = RouteStatsCalculator.speedColor(1f)
        val green = (color shr 8) and 0xFF
        val red = (color shr 16) and 0xFF
        assertEquals(255, red)
        assertEquals(0, green)
    }

    @Test
    fun `speedColor yellow for ratio 0_5`() {
        val color = RouteStatsCalculator.speedColor(0.5f)
        val green = (color shr 8) and 0xFF
        val red = (color shr 16) and 0xFF
        assertEquals(255, red)
        assertEquals(255, green)
    }
}
