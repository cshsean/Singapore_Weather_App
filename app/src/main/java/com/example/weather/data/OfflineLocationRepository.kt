package com.example.weather.data

import kotlinx.coroutines.flow.Flow

class OfflineLocationRepository(
    private val locationDao: LocationDao
) : LocationRepository {
    override fun getAllLocations(): Flow<List<LocationEntity>> =
        locationDao.getAllLocations()

    override fun searchLocations(query: String): Flow<List<LocationEntity>> =
        locationDao.searchLocations(query)
}
