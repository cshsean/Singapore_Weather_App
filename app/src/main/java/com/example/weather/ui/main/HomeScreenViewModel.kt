package com.example.weather.ui.main

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weather.WeatherApplication
import com.example.weather.data.NetworkWeatherInfoRepository
import com.example.weather.location.DefaultLocationClient
import com.example.weather.location.LocationClient
import com.example.weather.location.LocationService
import com.example.weather.model.UVIndexResponse
import com.example.weather.ui.main.getRegionFromArea
import com.example.weather.ui.main.utils.haversineDistance
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.*

sealed interface WeatherUiState {
    data class Success(
        val location: String,
        val currentWeather: String,
        val UVIndex: Int,
        val hourlyForecast: List<HourlyForecastInfo>,
        val temperature: Double,
        val windSpeed: Float,
        val rainfall: Double
    ) : WeatherUiState

    object Error : WeatherUiState
    object Loading : WeatherUiState
}

data class HourlyForecastInfo(
    val time: String,         // e.g. "Morning", "Afternoon", etc.
    val weatherStatus: String
)

@RequiresApi(Build.VERSION_CODES.O)
class HomeScreenViewModel(
    private val networkWeatherInfoRepository: NetworkWeatherInfoRepository,
    private val locationClient: LocationClient
) : ViewModel() {
    var weatherUiState: WeatherUiState by mutableStateOf(WeatherUiState.Loading)
        private set

    private var isInitialLoad = true
    private var fetchingLocation = false

    init {
        observeGpsState()
        fetchLocation()
    }

    // Refresh Screen Logic
    var isRefreshing by mutableStateOf(false)
        private set

    fun refresh() {
        isRefreshing = true
        fetchLocation()
        // You can delay or listen to fetch completion to reset
        viewModelScope.launch {
            delay(1000)
            isRefreshing = false
        }
    }

    fun fetchLocation() {
        if (fetchingLocation) return
        fetchingLocation = true

        viewModelScope.launch {
            try {
                val location = locationClient.getLocationUpdate(10_000L).first()
                Log.d("HomeScreenViewModel", "Location: $location")
                getLocation(location.latitude, location.longitude)
            } catch (e: LocationClient.LocationException) {
                Log.e("HomeScreenViewModel", "Location error", e)
                weatherUiState = WeatherUiState.Error
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Unexpected error", e)
                weatherUiState = WeatherUiState.Error
            } finally {
                fetchingLocation = false
            }
        }
    }

    fun getLocation(userLat: Double, userLong: Double) {
        viewModelScope.launch {
            if (isInitialLoad) {
                weatherUiState = WeatherUiState.Loading
            }
            weatherUiState = try {
                val twoHourForecastResponse = networkWeatherInfoRepository.getTwoHourForecast()
                val UVIndexResponse = networkWeatherInfoRepository.getUVIndex()
                val forecast24hResponse = networkWeatherInfoRepository.get24HourForecast()
                val temperatureResponse = networkWeatherInfoRepository.getTemperature()
                val windSpeedResponse = networkWeatherInfoRepository.getWindSpeed()
                val rainfallResponse = networkWeatherInfoRepository.getRainfall()

                if (twoHourForecastResponse.areaMetaData.isEmpty()) {
                    WeatherUiState.Error
                } else {
                    val nearestArea = twoHourForecastResponse.areaMetaData.minByOrNull { area ->
                        haversineDistance(
                            userLat,
                            userLong,
                            area.labelLocation.latitude,
                            area.labelLocation.longitude
                        )
                    }

                    val minDistance = nearestArea?.let {
                        haversineDistance(
                            userLat,
                            userLong,
                            it.labelLocation.latitude,
                            it.labelLocation.longitude
                        )
                    } ?: Double.MAX_VALUE

                    val areaName = if (minDistance > 5.0) {
                        "Outside SG"
                    } else {
                        nearestArea?.name ?: "Unknown Location"
                    }

                    val currentWeather = twoHourForecastResponse.items[0].forecasts.find {
                        it.area == nearestArea?.name
                    }?.forecast ?: ""

                    val currentUVIndex = UVIndexResponse.items[0].index[0].value
                    val region = getRegionFromArea(areaName)

                    val hourlyForecastList = buildList {
                        val periods = forecast24hResponse.items.firstOrNull()?.periods ?: emptyList()
                        val timeSlots = listOf(
                            "06:00" to Pair(LocalTime.of(6, 0), LocalTime.of(12, 0)),
                            "12:00" to Pair(LocalTime.of(12, 0), LocalTime.of(18, 0)),
                            "18:00" to Pair(LocalTime.of(18, 0), LocalTime.MIDNIGHT),
                            "00:00" to Pair(LocalTime.MIDNIGHT, LocalTime.of(6, 0))
                        )

                        for ((label, slotRange) in timeSlots) {
                            val matchedPeriod = periods.find { period ->
                                val start = OffsetDateTime.parse(period.time.start).toLocalTime()
                                val end = OffsetDateTime.parse(period.time.end).toLocalTime()
                                if (slotRange.first < slotRange.second) {
                                    start < slotRange.second && end > slotRange.first
                                } else {
                                    start >= slotRange.first || end <= slotRange.second
                                }
                            }

                            val weatherStatus = when (region) {
                                "north" -> matchedPeriod?.regions?.north
                                "south" -> matchedPeriod?.regions?.south
                                "east" -> matchedPeriod?.regions?.east
                                "west" -> matchedPeriod?.regions?.west
                                "central" -> matchedPeriod?.regions?.central
                                else -> null
                            } ?: "Unknown"

                            add(HourlyForecastInfo(label, weatherStatus))
                        }
                    }

                    val closestStation = temperatureResponse.metadata.stations.minByOrNull { station ->
                        haversineDistance(userLat, userLong, station.location.latitude, station.location.longitude)
                    }

                    val temperature = temperatureResponse.items.firstOrNull()?.readings
                        ?.find { it.stationId == closestStation?.id }
                        ?.value ?: -1.0

                    val closestWindStation = windSpeedResponse.metadata.stations.minByOrNull { station ->
                        haversineDistance(userLat, userLong, station.location.latitude, station.location.longitude)
                    }

                    val windSpeed = windSpeedResponse.items.firstOrNull()?.readings
                        ?.find { it.stationId == closestWindStation?.id }
                        ?.value ?: -1f

                    val closestRainfallStation = rainfallResponse.metadata.stations.minByOrNull { station ->
                        haversineDistance(userLat, userLong, station.location.latitude, station.location.longitude)
                    }

                    val rainfall = rainfallResponse.items.firstOrNull()?.readings
                        ?.find { it.stationId == closestRainfallStation?.id }
                        ?.value ?: 0.0

                    isInitialLoad = false

                    WeatherUiState.Success(
                        areaName,
                        currentWeather,
                        currentUVIndex,
                        hourlyForecastList,
                        temperature,
                        windSpeed,
                        rainfall
                    )
                }
            } catch (e: Exception) {
                isInitialLoad = false
                Log.e("HomeScreenViewModel", "Error fetching weather: ${e.message}", e)
                WeatherUiState.Error
            }
        }
    }

    private fun observeGpsState() {
        viewModelScope.launch {
            locationClient.locationStatusFlow.collect { isEnabled ->
                if (!isEnabled) {
                    weatherUiState = WeatherUiState.Error
                } else if (weatherUiState == WeatherUiState.Error && !fetchingLocation) {
                    fetchLocation()
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WeatherApplication)
                val weatherInfoRepository = application.container.networkWeatherInfoRepository
                val locationClient = DefaultLocationClient(
                    application,
                    LocationServices.getFusedLocationProviderClient(application)
                )
                HomeScreenViewModel(weatherInfoRepository, locationClient)
            }
        }
    }
}