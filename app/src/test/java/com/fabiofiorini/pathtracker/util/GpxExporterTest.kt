package com.fabiofiorini.pathtracker.util

import com.fabiofiorini.pathtracker.data.RouteEntity
import com.fabiofiorini.pathtracker.data.RoutePointEntity
import org.junit.Assert.*
import org.junit.Test

class GpxExporterTest {

    @Test
    fun `buildGpx returns null for empty points`() {
        val route = RouteEntity(
            title = "Test", distanceKm = 1f, durationSec = 60L,
            averageSpeedKmh = 60f, date = 1000L
        )
        assertNull(GpxExporter.buildGpx(route, emptyList()))
    }

    @Test
    fun `buildGpx produces valid GPX structure`() {
        val route = RouteEntity(
            title = "Gita al lago", distanceKm = 5f, durationSec = 1800L,
            averageSpeedKmh = 10f, date = 1000L
        )
        val points = listOf(
            RoutePointEntity(routeId = 1, orderIndex = 0, lat = 45.0, lon = 9.0, timestampSec = 1),
            RoutePointEntity(routeId = 1, orderIndex = 1, lat = 45.1, lon = 9.1, timestampSec = 2)
        )

        val result = GpxExporter.buildGpx(route, points)

        assertNotNull(result)
        assertTrue(result!!.contains("<?xml version=\"1.0\""))
        assertTrue(result.contains("<gpx version=\"1.1\""))
        assertTrue(result.contains("<name>Gita al lago</name>"))
        assertTrue(result.contains("<trkpt lat=\"45.0\" lon=\"9.0\">"))
        assertTrue(result.contains("<trkpt lat=\"45.1\" lon=\"9.1\">"))
        assertTrue(result.contains("</trkseg>"))
        assertTrue(result.contains("</trk>"))
        assertTrue(result.contains("</gpx>"))
    }

    @Test
    fun `sanitizeFileName replaces illegal characters`() {
        val result = GpxExporter.sanitizeFileName("Percorso: test/file.xml")
        assertEquals("Percorso_ test_file.xml", result)
    }

    @Test
    fun `sanitizeFileName truncates long names`() {
        val longName = "A".repeat(200)
        val result = GpxExporter.sanitizeFileName(longName)
        assertEquals(100, result.length)
    }

    @Test
    fun `escapeXml handles all five entities`() {
        val result = GpxExporter.escapeXml("\"AT&T's <new> & <old>\"")
        assertEquals("&quot;AT&amp;T&apos;s &lt;new&gt; &amp; &lt;old&gt;&quot;", result)
    }
}
