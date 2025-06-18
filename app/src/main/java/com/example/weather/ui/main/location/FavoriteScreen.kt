package com.example.weather.ui.main.location

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.data.FavoriteEntity
import com.example.weather.data.LocationEntity
import com.example.weather.ui.main.getWeatherImageRes
import com.google.gson.Gson

@Composable
fun FavoriteScreen(
    favoriteList: List<FavoriteEntity>,
    onItemClick: (FavoriteEntity) -> Unit,
    viewModel: SearchLocationViewModel,
    modifier: Modifier = Modifier
) {
    val weatherImageCache = remember { mutableStateMapOf<String, Int>() }
    val weatherDescriptionCache = remember { mutableStateMapOf<String, String>() }

    if (favoriteList.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "no favorites added yet",
                color = Color.White,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.weight(2f))
        }
    } else {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(-(5).dp),
            contentPadding = PaddingValues(horizontal = 18.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(
                items = favoriteList,
                key = { favoriteList -> favoriteList.name }
            ) { favorite ->
                FavoriteItem(
                    location = favorite,
                    onClick = { onItemClick(favorite) },
                    weatherImageCache = weatherImageCache,
                    weatherDescriptionCache = weatherDescriptionCache,
                    viewModel = viewModel
                )
            }
            item{
                Spacer(modifier = Modifier.height(150.dp))
            }
        }
    }
}

@Composable
fun FavoriteItem(
    location: FavoriteEntity,
    onClick: () -> Unit,
    viewModel: SearchLocationViewModel,
    weatherImageCache: MutableMap<String, Int>,
    weatherDescriptionCache: MutableMap<String, String>,
    modifier: Modifier = Modifier
) {
    val lat = location.latitude
    val lon = location.longitude

    val weatherImageRes = remember { mutableStateOf<Int?>(null) }
    val weatherDescriptionRes = remember {mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(location.name) {
        isLoading.value = true
        try {
            val forecast = viewModel.networkWeatherInfoRepository.getTwoHourForecast()
            val nearestArea = forecast.areaMetaData.minByOrNull {
                viewModel.haversineDistance(
                    lat,
                    lon,
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
            Log.e("FavoriteItem", "Error: ${e.message}")
            weatherImageRes.value = getWeatherImageRes("Unknown")
        } finally {
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
                Text(
                    text = if (weatherDescriptionRes.value == null) "" else weatherDescriptionRes.value.toString(),
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            when {
                isLoading.value -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
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
                            .size(20.dp)
                            .padding(end = 30.dp)
                    )
                }
            }
        }
    }
}

