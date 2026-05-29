package com.fabiofiorini.traveltracker.util

import org.junit.Assert.*
import org.junit.Test
import org.osmdroid.util.GeoPoint

class RouteSimplifierTest {

    @Test
    fun `simplify returns all indices for 2 points`() {
        val points = listOf(
            GeoPoint(45.0, 9.0),
            GeoPoint(45.001, 9.001)
        )
        val indices = RouteSimplifier.simplify(points)
        assertEquals(listOf(0, 1), indices)
    }

    @Test
    fun `simplify keeps endpoints for collinear points`() {
        val points = listOf(
            GeoPoint(45.0, 9.0),
            GeoPoint(45.0001, 9.0001),
            GeoPoint(45.0002, 9.0002),
            GeoPoint(45.0003, 9.0003)
        )
        val indices = RouteSimplifier.simplify(points)
        assertEquals(listOf(0, 3), indices)
    }

    @Test
    fun `simplify keeps deviating point above tolerance`() {
        val points = listOf(
            GeoPoint(45.0, 9.0),
            GeoPoint(45.0005, 9.0005),
            GeoPoint(45.0001, 9.0010)
        )
        val indices = RouteSimplifier.simplify(points, toleranceMeters = 10.0)
        assertTrue(indices.contains(1))
    }

    @Test
    fun `simplify removes colinear intermediate point`() {
        val points = listOf(
            GeoPoint(45.0, 9.0),
            GeoPoint(45.0005, 9.0005),
            GeoPoint(45.0010, 9.0010)
        )
        val indices = RouteSimplifier.simplify(points, toleranceMeters = 100.0)
        assertEquals(listOf(0, 2), indices)
    }

    @Test
    fun `perpendicularDistance returns zero for collinear point`() {
        val dist = RouteSimplifier.perpendicularDistance(
            45.0, 9.0,
            45.0, 9.0,
            45.001, 9.001
        )
        assertEquals(0.0, dist, 0.01)
    }

    @Test
    fun `perpendicularDistance returns positive for off-line point`() {
        val dist = RouteSimplifier.perpendicularDistance(
            45.0005, 9.0005,
            45.0, 9.0,
            45.001, 9.001
        )
        assertTrue(dist > 0.0)
    }
}
