package com.fabiofiorini.traveltracker.data

import androidx.room.*

@Dao
interface RouteDao {

    @Insert
    suspend fun insertRoute(route: RouteEntity): Long

    @Insert
    suspend fun insertPoints(points: List<RoutePointEntity>)

    @Query("SELECT * FROM routes ORDER BY date DESC")
    suspend fun getAllRoutes(): List<RouteEntity>
}