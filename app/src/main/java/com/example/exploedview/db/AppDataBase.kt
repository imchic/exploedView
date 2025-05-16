package com.example.exploedview.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// AppDatabase.kt
@Database(entities = [BuildingInfo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buildingInfoDao(): BuildingInfoDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user-database"
                ).build()
            }
            return INSTANCE!!
        }
    }
}