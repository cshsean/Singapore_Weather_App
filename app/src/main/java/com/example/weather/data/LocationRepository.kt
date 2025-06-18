package com.example.weather.data

import com.example.weather.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface LocationRepository {
    fun getAllLocations(): Flow<List<LocationEntity>>
    fun searchLocations(query: String): Flow<List<LocationEntity>>
}




