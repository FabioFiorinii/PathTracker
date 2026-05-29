package com.fabiofiorini.pathtracker.viewmodel

import kotlinx.coroutines.flow.Flow
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fabiofiorini.pathtracker.data.DatabaseProvider
import com.fabiofiorini.pathtracker.data.RouteEntity
import com.fabiofiorini.pathtracker.data.RoutePointEntity
import com.fabiofiorini.pathtracker.repository.TrackingRepository
import com.fabiofiorini.pathtracker.tracking.TrackingManager
import com.fabiofiorini.pathtracker.util.RouteSimplifier
import kotlinx.coroutines.launch

class TrackingViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: TrackingRepository? = null,
    val trackingManager: TrackingManager = TrackingManager.current
) : AndroidViewModel(application) {

    private val repo: TrackingRepository

    val routes: Flow<List<RouteEntity>>

    val routeCount: Flow<Int>

    val totalDistanceKm: Flow<Float>

    val totalDurationSec: Flow<Long>

    val routePoints = trackingManager.points

    val elapsedSeconds = trackingManager.elapsedSeconds

    val distanceMeters = trackingManager.distanceMeters

    init {
        repo = repository ?: run {
            val dao = DatabaseProvider
                .getDatabase(application)
                .routeDao()
            TrackingRepository(dao)
        }

        routes = repo.getAllRoutes()
        routeCount = repo.getRouteCount()
        totalDistanceKm = repo.getTotalDistanceKm()
        totalDurationSec = repo.getTotalDurationSec()
    }

    fun saveCurrentRoute(title: String) {

        viewModelScope.launch {

            val snapshotPoints = trackingManager.points.toList()
            val snapshotTimestamps = trackingManager.timestamps.toList()
            val snapshotElapsed = trackingManager.elapsedSeconds.longValue
            val snapshotDistance = trackingManager.distanceMeters.floatValue

            val avgSpeed =
                if (snapshotElapsed > 0)
                    (snapshotDistance / 1000f) /
                            (snapshotElapsed / 3600f)
                else 0f

            try {
                val route = RouteEntity(
                    title = title,
                    date = System.currentTimeMillis(),
                    durationSec = snapshotElapsed,
                    distanceKm = snapshotDistance / 1000f,
                    averageSpeedKmh = avgSpeed
                )

                val routeId = repo.saveRoute(route)

                val keepIndices = RouteSimplifier.simplify(snapshotPoints)

                val points = keepIndices.mapIndexed { orderIndex, originalIndex ->
                    RoutePointEntity(
                        routeId = routeId,
                        orderIndex = orderIndex,
                        lat = snapshotPoints[originalIndex].latitude,
                        lon = snapshotPoints[originalIndex].longitude,
                        timestampSec = (snapshotTimestamps.getOrElse(originalIndex) { System.currentTimeMillis() } / 1000).toInt()
                    )
                }

                repo.savePoints(points)
            } finally {
                trackingManager.reset()
            }
        }
    }

    fun deleteRoute(route: RouteEntity) {

        viewModelScope.launch {
            repo.deletePoints(route.id)
            repo.deleteRouteById(route.id)
        }
    }

    suspend fun getPoints(routeId: Long) =
        repo.getPoints(routeId)

    suspend fun getRoute(routeId: Long) =
        repo.getRoute(routeId)
}
