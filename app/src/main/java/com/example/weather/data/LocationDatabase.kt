package com.example.weather.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LocationEntity::class, FavoriteEntity::class],
    version = 5,
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var Instance: LocationDatabase? = null

        fun getDatabase(context: Context): LocationDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    LocationDatabase::class.java,
                    "location_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

