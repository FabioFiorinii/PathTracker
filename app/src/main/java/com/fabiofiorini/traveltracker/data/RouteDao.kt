package com.fabiofiorini.traveltracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RouteDao {

    @Insert
    suspend fun insertRoute(route: RouteEntity): Long

    @Insert
    suspend fun insertPoints(points: List<RoutePointEntity>)

    @Query("SELECT * FROM routes ORDER BY date DESC")
    suspend fun getAllRoutes(): List<RouteEntity>

    @Query("SELECT * FROM route_points WHERE routeId = :routeId ORDER BY timestamp ASC")
    suspend fun getRoutePoints(routeId: Long): List<RoutePointEntity>

    @Delete
    suspend fun deleteRoute(route: RouteEntity)

    @Query("DELETE FROM route_points WHERE routeId = :routeId")
    suspend fun deletePointsByRoute(routeId: Long)
}