package com.fabiofiorini.traveltracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RouteEntity::class, RoutePointEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
}