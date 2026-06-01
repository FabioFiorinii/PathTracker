package com.fabiofiorini.pathtracker.data

import android.content.Context
import android.util.Log
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
        } catch (e: Exception) {
            Log.e("DatabaseProvider", "Vacuum failed", e)
        }
    }
}
