package com.example.weather.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY name")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun searchLocations(query: String): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations ORDER BY name")
    suspend fun getAllLocationsOnce(): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)
}

