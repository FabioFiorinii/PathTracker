package com.fabiofiorini.traveltracker.util

import org.osmdroid.util.GeoPoint
import kotlin.math.*

object RouteSimplifier {

    fun simplify(
        points: List<GeoPoint>,
        toleranceMeters: Double = 5.0
    ): List<Int> {
        if (points.size <= 2) return points.indices.toList()

        val keep = BooleanArray(points.size)
        keep[0] = true
        keep[points.size - 1] = true

        douglasPeucker(points, 0, points.size - 1, keep, toleranceMeters)

        return keep.indices.filter { keep[it] }
    }

    private fun douglasPeucker(
        points: List<GeoPoint>,
        first: Int,
        last: Int,
        keep: BooleanArray,
        toleranceMeters: Double
    ) {
        if (last - first <= 1) return

        var maxDist = 0.0
        var maxIdx = first

        for (i in first + 1 until last) {
            val dist = perpendicularDistance(
                points[i].latitude, points[i].longitude,
                points[first].latitude, points[first].longitude,
                points[last].latitude, points[last].longitude
            )
            if (dist > maxDist) {
                maxDist = dist
                maxIdx = i
            }
        }

        if (maxDist > toleranceMeters) {
            keep[maxIdx] = true
            douglasPeucker(points, first, maxIdx, keep, toleranceMeters)
            douglasPeucker(points, maxIdx, last, keep, toleranceMeters)
        }
    }

    internal fun perpendicularDistance(
        pLat: Double, pLon: Double,
        aLat: Double, aLon: Double,
        bLat: Double, bLon: Double
    ): Double {
        val mPerDegLat = 111_111.0
        val avgLat = Math.toRadians((aLat + bLat + pLat) / 3.0)
        val mPerDegLon = 111_111.0 * cos(avgLat)

        val ax = aLon * mPerDegLon
        val ay = aLat * mPerDegLat
        val bx = bLon * mPerDegLon
        val by = bLat * mPerDegLat
        val px = pLon * mPerDegLon
        val py = pLat * mPerDegLat

        val dx = bx - ax
        val dy = by - ay

        if (dx == 0.0 && dy == 0.0) {
            return sqrt((px - ax) * (px - ax) + (py - ay) * (py - ay))
        }

        val t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)
        val clampedT = t.coerceIn(0.0, 1.0)

        val projX = ax + clampedT * dx
        val projY = ay + clampedT * dy

        return sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY))
    }
}
