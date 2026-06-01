package com.fabiofiorini.pathtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val distanceKm: Float,
    val durationSec: Long,
    val averageSpeedKmh: Float,
    val date: Long,
    val steps: Int = 0
)