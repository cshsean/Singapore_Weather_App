package com.example.weather.ui.main.location

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weather.WeatherApplication
import com.example.weather.data.FavoriteEntity
import com.example.weather.data.FavoriteRepository
import com.example.weather.data.LocationEntity
import com.example.weather.data.LocationRepository
import com.example.weather.data.NetworkWeatherInfoRepository
import com.example.weather.ui.main.HourlyForecastInfo
import com.example.weather.ui.main.getRegionFromArea
import com.example.weather.ui.main.utils.haversineDistance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull


sealed interface MockWeatherUiState {
    data class Success(
        val location: String,
        val currentWeather: String,
        val UVIndex: Int,
        val hourlyForecast: List<HourlyForecastInfo>,
        val temperature: Double,
        val windSpeed: Float,
        val rainfall: Double
    ) : MockWeatherUiState

    object Error : MockWeatherUiState
    object Loading : MockWeatherUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchLocationViewModel(
    private val locationRepo: LocationRepository,
    internal val networkWeatherInfoRepository: NetworkWeatherInfoRepository,
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {
    private var searchText by mutableStateOf("")

    fun retrieveSearchText(): String {
        return searchText
    }

    fun updateSearchText(searchInput: String) {
        searchText = searchInput.trim()
    }

    fun searchLocations(): Flow<List<LocationEntity>> {
        return locationRepo.searchLocations(searchText)
    }

    fun getFavorites(): Flow<List<FavoriteEntity>> {
        return favoriteRepo.getFavorites()
    }

    // Refresh Screen Logic
    var isRefreshing by mutableStateOf(false)
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh(lat: Double, lon: Double) {
        isRefreshing = true
        viewModelScope.launch {
            getMockLocation(lat, lon)
            delay(1000)
            isRefreshing = false
        }
    }

    // For Testing
    fun getAllLocationList(): Flow<List<LocationEntity>> {
        viewModelScope.launch {
            locationRepo.getAllLocations().collect { list ->
                Log.d("SearchLocationScreen", "getAllLocations: ${list.map { it.name }}")
            }
        }
        return locationRepo.getAllLocations()
    }

    // New: A private MutableStateFlow to hold the currently selected location name
    private val _currentLocationName = MutableStateFlow<String?>(null)

    // Exposed StateFlow for the favorite status, derived from _currentLocationName
    val isFavorite: StateFlow<Boolean> = _currentLocationName
        .filterNotNull() // Only proceed when a location name is available
        .flatMapLatest { name -> // Use flatMapLatest to switch to the new favorite status flow
            favoriteRepo.isFavorite(name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Call this when the location changes to update the _currentLocationName flow
    fun updateCurrentLocationName(name: String?) {
        _currentLocationName.value = name
    }

    var isFavoriteUpdating by mutableStateOf(false)
        private set // Make this private set to control updates

    fun toggleFavorite(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            if (name.isEmpty() || isFavoriteUpdating) return@launch

            isFavoriteUpdating = true
            try {
                val currentFavorites = favoriteRepo.getFavorites().firstOrNull() ?: emptyList()

                val isAlreadyFavorited = currentFavorites.any { it.name.equals(name, ignoreCase = true) }

                if (isAlreadyFavorited) {
                    favoriteRepo.removeFavorite(name)
                } else {
                    // Prevent duplicate insert
                    if (!currentFavorites.any { it.name.equals(name, ignoreCase = true) }) {
                        favoriteRepo.addFavorite(name, latitude, longitude)
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchLocationViewModel", "Error toggling favorite: ${e.message}", e)
            } finally {
                isFavoriteUpdating = false
            }
        }
    }

    // Mock Location Screen Logic
    private var isInitialLoad = true

    var mockWeatherUiState: MockWeatherUiState by mutableStateOf(MockWeatherUiState.Loading)
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMockLocation(
        userLat: Double,
        userLong: Double
    ) {
        mockWeatherUiState = MockWeatherUiState.Loading
        viewModelScope.launch {
            if (isInitialLoad) {
                mockWeatherUiState = MockWeatherUiState.Loading
            }
            mockWeatherUiState = try {
                val twoHourForecastResponse = networkWeatherInfoRepository.getTwoHourForecast()
                val UVIndexResponse = networkWeatherInfoRepository.getUVIndex()
                val forecast24hResponse = networkWeatherInfoRepository.get24HourForecast()
                val temperatureResponse = networkWeatherInfoRepository.getTemperature() // ✅
                val windSpeedResponse = networkWeatherInfoRepository.getWindSpeed() // ✅
                val rainfallResponse = networkWeatherInfoRepository.getRainfall()  // <-- fetch rainfall here

                if (twoHourForecastResponse.areaMetaData.isEmpty()) {
                    MockWeatherUiState.Error
                } else {
                    // GET NAME OF NEAREST AREA
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

                    // CHECK IF USER OUTSIDE SINGAPORE (>5KM)
                    val areaName = if (minDistance > 5.0) {
                        "Outside SG"
                    } else {
                        nearestArea?.name ?: "Unknown Location"
                    }

                    // GET WEATHER FROM LOCATION OF USER
                    val currentWeather = twoHourForecastResponse.items[0].forecasts.find { it.area == nearestArea?.name }?.forecast ?: ""

                    // GET UV INDEX
                    val currentUVIndex = UVIndexResponse.items[0].index[0].value

                    // GET 24 HOUR FORECAST LIST
                    val region = getRegionFromArea(areaName)

                    val hourlyForecastList = buildList {
                        val periods = forecast24hResponse.items.firstOrNull()?.periods ?: emptyList()

                        // Define 4 slots
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
                                // Handle wrapping midnight
                                if (slotRange.first < slotRange.second) {
                                    start < slotRange.second && end > slotRange.first
                                } else {
                                    // Handles Early Morning slot (00:00–06:00)
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


                    // FIND NEAREST TEMPERATURE STATION
                    val closestStation = temperatureResponse.metadata.stations.minByOrNull { station ->
                        haversineDistance(
                            userLat,
                            userLong,
                            station.location.latitude,
                            station.location.longitude
                        )
                    }

                    val closestStationId = closestStation?.id
                    val temperature = temperatureResponse.items.firstOrNull()?.readings
                        ?.find { it.stationId == closestStationId }
                        ?.value ?: -1.0 // Fallback temp

                    // FIND NEAREST WIND STATION
                    val closestWindStation = windSpeedResponse.metadata.stations.minByOrNull { station ->
                        haversineDistance(
                            userLat,
                            userLong,
                            station.location.latitude,
                            station.location.longitude
                        )
                    }

                    val closestWindStationId = closestWindStation?.id

                    val windSpeed = windSpeedResponse.items.firstOrNull()?.readings
                        ?.find { it.stationId == closestWindStationId }
                        ?.value ?: -1f // fallback

                    // FIND RAINFALL
                    val closestRainfallStation = rainfallResponse.metadata.stations.minByOrNull { station ->
                        haversineDistance(
                            userLat,
                            userLong,
                            station.location.latitude,
                            station.location.longitude
                        )
                    }

                    val closestRainfallStationId = closestRainfallStation?.id
                    val rainfall = rainfallResponse.items.firstOrNull()?.readings
                        ?.find { it.stationId == closestRainfallStationId }
                        ?.value ?: 0.0  // Default rainfall if missing

                    isInitialLoad = false // ✅ Set flag after first success

                    MockWeatherUiState.Success(
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
                MockWeatherUiState.Error
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[AndroidViewModelFactory.APPLICATION_KEY] as WeatherApplication)
                val locationRepository = application.container.locationRepository
                val weatherInfoRepository = application.container.networkWeatherInfoRepository
                val favoriteRepository = application.container.favoriteRepository

                SearchLocationViewModel(
                    locationRepo = locationRepository,
                    networkWeatherInfoRepository = weatherInfoRepository,
                    favoriteRepo = favoriteRepository
                )
            }
        }
    }
}

