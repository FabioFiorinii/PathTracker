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
    application: Application
) : AndroidViewModel(application) {

    private val repository: TrackingRepository

    val routes: Flow<List<RouteEntity>>

    val routePoints = TrackingManager.points

    val elapsedSeconds = TrackingManager.elapsedSeconds

    val distanceMeters = TrackingManager.distanceMeters

    init {

        val dao = DatabaseProvider
            .getDatabase(application)
            .routeDao()

        repository = TrackingRepository(dao)

        routes = repository.getAllRoutes()
    }

    fun saveCurrentRoute(title: String) {

        viewModelScope.launch {

            val avgSpeed =
                if (TrackingManager.elapsedSeconds.longValue > 0)
                    (TrackingManager.distanceMeters.floatValue / 1000f) /
                            (TrackingManager.elapsedSeconds.longValue / 3600f)
                else 0f
            val route = RouteEntity(
                title = title,
                date = System.currentTimeMillis(),
                durationSec = TrackingManager.elapsedSeconds.longValue,
                distanceKm = TrackingManager.distanceMeters.floatValue / 1000f,
                averageSpeedKmh = avgSpeed
            )

            val routeId = repository.saveRoute(route)

            val points = TrackingManager.points.map {
                RoutePointEntity(
                    routeId = routeId,
                    lat = it.latitude,
                    lon = it.longitude,
                    timestamp = System.currentTimeMillis()
                )
            }

            repository.savePoints(points)

            TrackingManager.reset()
        }
    }

    fun deleteRoute(route: RouteEntity) {

        viewModelScope.launch {

            repository.deletePoints(route.id)
            repository.deleteRoute(route)
        }
    }

    suspend fun getPoints(routeId: Long) =
        repository.getPoints(routeId)

    suspend fun getRoute(routeId: Long) =
        repository.getRoute(routeId)
}