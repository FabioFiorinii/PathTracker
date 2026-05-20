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

    suspend fun deleteRoute(route: RouteEntity) {
        dao.deleteRoute(route)
    }

    suspend fun deletePoints(routeId: Long) {
        dao.deletePointsByRoute(routeId)
    }

    fun getAllRoutes() = dao.getAllRoutes()

    suspend fun getPoints(routeId: Long) =
        dao.getPointsForRoute(routeId)
}