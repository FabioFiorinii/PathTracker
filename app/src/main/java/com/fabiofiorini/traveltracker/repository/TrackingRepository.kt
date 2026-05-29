package com.fabiofiorini.traveltracker.repository

import com.fabiofiorini.traveltracker.data.RouteDao
import com.fabiofiorini.traveltracker.data.RouteEntity
import com.fabiofiorini.traveltracker.data.RoutePointEntity

class TrackingRepository(
    private val dao: RouteDao
) {

    suspend fun saveRoute(
        route: RouteEntity
    ): Long {
        return dao.insertRoute(route)
    }

    suspend fun savePoints(
        points: List<RoutePointEntity>
    ) {
        dao.insertPoints(points)
    }

    suspend fun deleteRouteById(routeId: Long) {
        dao.deleteRouteById(routeId)
    }

    suspend fun deletePoints(routeId: Long) {
        dao.deletePointsByRoute(routeId)
    }

    fun getAllRoutes() = dao.getAllRoutes()

    suspend fun getPoints(routeId: Long) =
        dao.getPointsForRoute(routeId)

    suspend fun getRoute(routeId: Long) =
        dao.getRouteById(routeId)

    fun getRouteCount() = dao.getRouteCount()

    fun getTotalDistanceKm() = dao.getTotalDistanceKm()

    fun getTotalDurationSec() = dao.getTotalDurationSec()
}