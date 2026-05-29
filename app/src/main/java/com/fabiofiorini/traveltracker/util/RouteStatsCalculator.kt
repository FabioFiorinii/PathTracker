package com.fabiofiorini.traveltracker.util

import com.fabiofiorini.traveltracker.data.RoutePointEntity
import kotlin.math.*

object RouteStatsCalculator {

    fun calculateSpeeds(points: List<RoutePointEntity>): FloatArray {
        if (points.size < 2) return FloatArray(0)
        val speeds = FloatArray(points.size - 1)
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val distKm = haversineKm(p1.lat, p1.lon, p2.lat, p2.lon)
            val timeHours = (p2.timestampSec - p1.timestampSec) / 3600f
            speeds[i] = if (timeHours > 0) (distKm / timeHours).toFloat() else 0f
        }
        return speeds
    }

    fun speedColor(ratio: Float): Int {
        val alpha = 0xFF shl 24
        return when {
            ratio < 0.5f -> {
                val r = (ratio / 0.5f * 255).toInt().coerceIn(0, 255)
                alpha or (r shl 16) or (0xFF shl 8)
            }
            else -> {
                val g = ((1f - ratio) / 0.5f * 255).toInt().coerceIn(0, 255)
                alpha or (0xFF shl 16) or (g shl 8)
            }
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
