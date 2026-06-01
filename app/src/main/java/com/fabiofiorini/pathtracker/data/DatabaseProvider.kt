package com.fabiofiorini.pathtracker.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        var inst = INSTANCE
        if (inst == null) {
            synchronized(this) {
                inst = INSTANCE
                if (inst == null) {
                    inst = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "travel_db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = inst
                }
            }
        }
        return inst!!
    }

    fun vacuum() {
        try {
            val db = INSTANCE?.openHelper?.writableDatabase ?: return
            db.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
            db.execSQL("VACUUM")
        } catch (_: Exception) {
        }
    }
}
