package com.fabiofiorini.traveltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_points")
data class RoutePointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val routeId: Long,

    val lat: Double,
    val lon: Double,

    val timestamp: Long
)