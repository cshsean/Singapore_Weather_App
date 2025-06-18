package com.example.weather.ui.main.location

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.data.LocationEntity
import com.example.weather.ui.background.DynamicSkyBackground
import com.example.weather.ui.main.WeatherUiState
import com.example.weather.ui.main.getWeatherImageRes
import com.example.weather.ui.main.utils.haversineDistance
import kotlinx.coroutines.delay
import java.time.LocalTime


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ResultScreen(
    locationList: List<LocationEntity>,
    viewModel: SearchLocationViewModel,
    onItemClick: (LocationEntity) -> Unit
) {
    val weatherImageCache = remember { mutableStateMapOf<String, Int>() }
    val weatherDescriptionCache = remember { mutableStateMapOf<String, String>() }

    if (locationList.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "no location available :(",
                color = Color.White,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.weight(2f))
        }
    } else {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-5).dp),
            contentPadding = PaddingValues(horizontal = 18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(locationList, key = { it.name }) { location ->
                LocationItem(
                    location = location,
                    weatherImageCache = weatherImageCache,
                    weatherDescriptionCache = weatherDescriptionCache,
                    viewModel = viewModel,
                    onClick = { onItemClick(location) }
                )
            }

            item { Spacer(modifier = Modifier.height(150.dp)) }
        }
    }
}

/* WHAT WE CAN DO
* 1) INSERT THE LIST OF LOCATIONS IN A FUNCTION
* 2) THE FUNCTION WILL GET THE WEATHER FROM EACH ITEM IN LIST
* 3) THE FUNCTION WILL STORE THE WEATHER IN A LIST AND RETURN IT
* 4) PASS IN THE WEATHER TO EACH ITEM IN THE LIST OF LOCATIONS
* */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LocationItem(
    location: LocationEntity,
    viewModel: SearchLocationViewModel,
    weatherImageCache: MutableMap<String, Int>,
    weatherDescriptionCache: MutableMap<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weatherImageRes = remember { mutableStateOf<Int?>(null) }
    val weatherDescriptionRes = remember {mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(location.name) {
        if (!weatherImageCache.contains(location.name) && !weatherDescriptionCache.contains(location.name)) {
            isLoading.value = true
            try {
                val forecast = viewModel.networkWeatherInfoRepository.getTwoHourForecast()
                val nearestArea = forecast.areaMetaData.minByOrNull {
                    haversineDistance(
                        location.latitude,
                        location.longitude,
                        it.labelLocation.latitude,
                        it.labelLocation.longitude
                    )
                }

                val currentWeather = forecast.items[0].forecasts
                    .find { it.area == nearestArea?.name }
                    ?.forecast ?: "Unknown"

                val imageRes = getWeatherImageRes(currentWeather)
                weatherImageCache[location.name] = imageRes
                weatherImageRes.value = imageRes

                weatherDescriptionCache[location.name] = currentWeather
                weatherDescriptionRes.value = currentWeather
            } catch (e: Exception) {
                Log.e("LocationItem", "Error: ${e.message}")
                weatherImageRes.value = getWeatherImageRes("Unknown")
            } finally {
                isLoading.value = false
            }
        } else {
            weatherImageRes.value = weatherImageCache[location.name]
            weatherDescriptionRes.value = weatherDescriptionCache[location.name]
            isLoading.value = false
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .height(120.dp)
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            val locationText = when {
                !location.name.lowercase().contains("catchment") -> location.name
                location.name.lowercase().contains("central") -> "Central Water\nCatchment"
                else -> "Western Water\nCatchment"
            }

            val fontSize = if (locationText.contains("\n")) 24.sp else 28.sp

            Column(
                modifier = Modifier.padding(start = 25.dp)
            ) {
                Text(
                    text = locationText,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.1).sp,
                    lineHeight = if (locationText.contains("\n")) 30.sp else 35.sp,
                    maxLines = 2,
                    color = Color.Black
                )
                if (weatherDescriptionRes.value != null) {
                    Text(
                        text = weatherDescriptionRes.value.toString(),
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                } else {
                    var dotCount by remember { mutableStateOf(0) }

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(500L) // half-second interval
                            dotCount = (dotCount + 1) % 4 // cycles through 0 to 3
                        }
                    }

                    val loadingText = "Loading" + ".".repeat(dotCount)

                    Text(
                        text = loadingText,
                        fontSize = 15.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            when {
                isLoading.value -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(70.dp)
                            .padding(end = 30.dp),
                        strokeWidth = 2.dp
                    )
                }

                weatherImageRes.value != null -> {
                    AnimatedVisibility(
                        visible = !isLoading.value && weatherImageRes.value != null,
                        enter = fadeIn(animationSpec = tween(500)) +
                                slideInHorizontally(initialOffsetX = { it }),
                        exit = fadeOut()
                    ) {
                        Image(
                            painter = painterResource(weatherImageRes.value!!),
                            contentDescription = "Weather icon",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(horizontal = 25.dp)
                        )
                    }
                }

                else -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 30.dp)
                    )
                }
            }
        }
    }
}
