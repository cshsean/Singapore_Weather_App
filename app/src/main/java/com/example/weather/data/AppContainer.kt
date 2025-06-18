package com.example.weather.data

import android.content.Context
import com.example.weather.network.NEAApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
// import retrofit2.converter.scalars.ScalarsConverterFactory

interface AppContainer {
    val networkWeatherInfoRepository: NetworkWeatherInfoRepository
    val locationRepository: LocationRepository
    val favoriteRepository: FavoriteRepository
}

class DefaultAppContainer(private val context: Context): AppContainer {
    private val baseUrl = "https://api.data.gov.sg/v1/"

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json{
        ignoreUnknownKeys = true
        explicitNulls = false    // Treat missing fields as null
        coerceInputValues = true // Coerce invalid values to defaults
    }

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        // .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(baseUrl)
        .build()

    val retrofitService: NEAApiService by lazy {
        retrofit.create(NEAApiService::class.java)
    }

    override val networkWeatherInfoRepository: NetworkWeatherInfoRepository by lazy {
        NetworkWeatherInfoRepository(retrofitService)
    }

    override val locationRepository: LocationRepository by lazy {
        val db = LocationDatabase.getDatabase(context)
        OfflineLocationRepository(
            locationDao =  db.locationDao()
        )
    }

    override val favoriteRepository: FavoriteRepository by lazy {
        val db = LocationDatabase.getDatabase(context)
        OfflineFavoriteRepository(
            favoriteDao = db.favoriteDao()
        )
    }
}