package com.fabiofiorini.pathtracker.repository

import com.fabiofiorini.pathtracker.data.RouteDao
import com.fabiofiorini.pathtracker.data.RouteEntity
import com.fabiofiorini.pathtracker.data.RoutePointEntity

class TrackingRepository(
    private val dao: RouteDao
) {

    suspend fun saveRouteWithPoints(route: RouteEntity, points: (Long) -> List<RoutePointEntity>): Long {
        return dao.insertRouteWithPoints(route, points)
    }

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

    fun getTotalSteps() = dao.getTotalSteps()
}