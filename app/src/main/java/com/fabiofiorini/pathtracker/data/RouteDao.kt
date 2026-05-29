package com.fabiofiorini.pathtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Insert
    suspend fun insertRoute(route: RouteEntity): Long

    @Insert
    suspend fun insertPoints(points: List<RoutePointEntity>)

    @Query("SELECT * FROM routes ORDER BY date DESC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM route_points WHERE routeId = :routeId ORDER BY orderIndex ASC")
    suspend fun getRoutePoints(routeId: Long): List<RoutePointEntity>

    @Transaction
    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun deleteRouteById(routeId: Long)

    @Query("DELETE FROM route_points WHERE routeId = :routeId")
    suspend fun deletePointsByRoute(routeId: Long)

    @Query("SELECT * FROM route_points WHERE routeId = :routeId")
    suspend fun getPointsForRoute(
        routeId: Long
    ): List<RoutePointEntity>

    @Query("SELECT * FROM routes WHERE id = :routeId")
    suspend fun getRouteById(routeId: Long): RouteEntity?

    @Query("SELECT COUNT(*) FROM routes")
    fun getRouteCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(distanceKm), 0) FROM routes")
    fun getTotalDistanceKm(): Flow<Float>

    @Query("SELECT COALESCE(SUM(durationSec), 0) FROM routes")
    fun getTotalDurationSec(): Flow<Long>
}