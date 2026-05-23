package com.fabiofiorini.traveltracker.viewmodel

import kotlinx.coroutines.flow.Flow
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fabiofiorini.traveltracker.data.DatabaseProvider
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.data.RoutePointEntity
import com.fabiofiorini.traveltracker.repository.TrackingRepository
import com.fabiofiorini.traveltracker.tracking.TrackingManager
import kotlinx.coroutines.launch

class TrackingViewModel(
    application: Application,
    private val repository: TrackingRepository? = null
) : AndroidViewModel(application) {

    private val repo: TrackingRepository

    val trackingManager = TrackingManager().also {
        TrackingManager.current = it
    }

    val routes: Flow<List<RouteEntity>>

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
    }

    fun saveCurrentRoute(title: String) {

        viewModelScope.launch {

            val avgSpeed =
                if (trackingManager.elapsedSeconds.longValue > 0)
                    (trackingManager.distanceMeters.floatValue / 1000f) /
                            (trackingManager.elapsedSeconds.longValue / 3600f)
                else 0f
            val route = RouteEntity(
                title = title,
                date = System.currentTimeMillis(),
                durationSec = trackingManager.elapsedSeconds.longValue,
                distanceKm = trackingManager.distanceMeters.floatValue / 1000f,
                averageSpeedKmh = avgSpeed
            )

            val routeId = repo.saveRoute(route)

            val points = trackingManager.points.map {
                RoutePointEntity(
                    routeId = routeId,
                    lat = it.latitude,
                    lon = it.longitude,
                    timestamp = System.currentTimeMillis()
                )
            }

            repo.savePoints(points)

            trackingManager.reset()
        }
    }

    fun deleteRoute(route: RouteEntity) {

        viewModelScope.launch {

            repo.deletePoints(route.id)
            repo.deleteRoute(route)
        }
    }

    suspend fun getPoints(routeId: Long) =
        repo.getPoints(routeId)

    suspend fun getRoute(routeId: Long) =
        repo.getRoute(routeId)
}
