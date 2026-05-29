package com.fabiofiorini.traveltracker.data

import androidx.room.Entity

@Entity(tableName = "route_points", primaryKeys = ["routeId", "orderIndex"])
data class RoutePointEntity(
    val routeId: Long,
    val orderIndex: Int,
    val lat: Double,
    val lon: Double,
    val timestampSec: Int
)