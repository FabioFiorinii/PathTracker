package com.fabiofiorini.traveltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val distanceKm: Double,
    val durationSec: Int,
    val averageSpeedKmh: Double,
    val date: Long
)