package com.example.weather.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdate(interval: Long): Flow<Location>

    val locationStatusFlow: Flow<Boolean>

    class LocationException(message: String): Exception()
}