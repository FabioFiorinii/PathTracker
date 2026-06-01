package com.fabiofiorini.pathtracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RouteEntity::class, RoutePointEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
}