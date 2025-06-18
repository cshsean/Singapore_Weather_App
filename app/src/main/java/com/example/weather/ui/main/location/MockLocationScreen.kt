package com.example.weather.ui.main.location

import android.media.MediaPlayer
import androidx.compose.runtime.collectAsState
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.weather.data.LocationEntity
import com.example.weather.ui.background.DynamicSkyBackground
import com.example.weather.ui.main.HourlyForecastCard
import com.example.weather.ui.main.UVIndexSlider
import com.example.weather.ui.main.WindAndRainRow
import java.time.LocalTime
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.* // Make sure this import is present for `by` delegate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import com.example.weather.R
import com.example.weather.ui.main.SettingsScreenViewModel
import com.example.weather.ui.main.convertToImperial

// ... (Your existing imports) ...

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MockLocationScreen(
    location: LocationEntity?,
    navController: NavController,
    viewModel: SearchLocationViewModel,
    settingsScreenViewModel: SettingsScreenViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode = settingsScreenViewModel.isDarkMode
    val isImperialUnitEnabled = settingsScreenViewModel.isImperialUnitEnabled

    LaunchedEffect(location) {
        location?.let {
            viewModel.getMockLocation(it.latitude, it.longitude)
            viewModel.updateCurrentLocationName(it.name)
        } ?: viewModel.updateCurrentLocationName(null)
    }

    val isRefreshing = viewModel.isRefreshing
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            location?.let {
                viewModel.refresh(it.latitude, it.longitude)
            }
        }
    )

    val isFavorite by viewModel.isFavorite.collectAsState()
    val isFavoriteUpdating = viewModel.isFavoriteUpdating
    val mockWeatherUiState = viewModel.mockWeatherUiState

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        if (mockWeatherUiState is MockWeatherUiState.Success) {
            DynamicSkyBackground(
                currentTime = LocalTime.now(),
                currentWeather = mockWeatherUiState.currentWeather,
                isDarkMode = isDarkMode
            )
        } else {
            DynamicSkyBackground(
                currentTime = LocalTime.now(),
                isDarkMode = isDarkMode
            )
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Row: Cancel and Favorite
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(vertical = 25.dp, horizontal = 20.dp)
                        .weight(1f)
                ) {
                    Text(text = "Cancel", fontSize = 17.sp)
                }

                Button(
                    onClick = {
                        location?.let {
                            viewModel.toggleFavorite(it.name, it.latitude, it.longitude)
                        }
                    },
                    enabled = !isFavoriteUpdating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(vertical = 25.dp, horizontal = 20.dp)
                        .weight(1f)
                ) {
                    if (isFavoriteUpdating) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            text = if (isFavorite) "Remove" else "Add",
                            fontSize = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (mockWeatherUiState) {
                is MockWeatherUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading...", fontSize = 18.sp, color = Color.White)
                    }
                }

                is MockWeatherUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error fetching weather data.\nTurn on Location or recheck your\ninternet connection.",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                is MockWeatherUiState.Success -> {
                    val state = mockWeatherUiState

                    /** RAIN SOUND LOGIC **/
                    val context = LocalContext.current
                    val isRaining = state.currentWeather.lowercase().contains("rain") ||
                            state.currentWeather.lowercase().contains("shower")
                    val isRainSoundEnabled = settingsScreenViewModel.isRainSoundEnabled

// Manage MediaPlayer lifecycle
                    val shouldPlayRain = isRaining && isRainSoundEnabled

// Only create and manage MediaPlayer when needed
                    DisposableEffect(shouldPlayRain) {
                        var mediaPlayer: MediaPlayer? = null

                        if (shouldPlayRain) {
                            mediaPlayer = MediaPlayer.create(context, R.raw.rain_sound).apply {
                                isLooping = true
                                start()
                            }
                        }

                        onDispose {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                        }
                    }

                    /** MAIN COMPONENT **/
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(50.dp))

                        Text(
                            text = location?.name ?: "Unknown location",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.W300,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                lineHeight = 35.sp,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            modifier = Modifier.padding(horizontal = 50.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy((-12).dp)
                        ) {
                            val temperature = if (!isImperialUnitEnabled) {
                                state.temperature
                            } else {
                                convertToImperial(state.temperature, "Temperature")
                            }

                            Text(
                                text = temperature.toString(),
                                fontSize = 100.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp,
                                color = Color.White,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.2f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                            Text(
                                text = "°",
                                fontSize = 50.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.2f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                modifier = Modifier.offset(x = 8.dp, y = (-50).dp)
                            )
                        }

                        Text(
                            text = state.currentWeather,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W300,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = TextStyle(
                                lineHeight = 35.sp,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            modifier = Modifier.padding(
                                bottom = 100.dp,
                                start = 50.dp,
                                end = 50.dp
                            )
                        )

                        val uvValue = state.UVIndex.toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UVIndexSlider(value = uvValue, modifier = Modifier.widthIn(max = 400.dp))
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 25.dp)
                        ) {
                            Text(
                                text = "24 Hour Forecast",
                                fontSize = 16.sp,
                                color = Color.White,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .widthIn(max = 1000.dp)
                                    .padding(horizontal = 25.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.hourlyForecast) { item ->
                                    HourlyForecastCard(
                                        time = item.time,
                                        weatherStatus = item.weatherStatus
                                    )
                                }
                            }
                        }

                        WindAndRainRow(
                            windSpeed = state.windSpeed,
                            rainfall = state.rainfall,
                            isImperialUnitEnabled = isImperialUnitEnabled,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "credits for weather icon and rain animation\n" +
                                    "goes to iconixar and douglas schatz respectively",
                            fontSize = 10.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = TextStyle(lineHeight = 15.sp),
                            modifier = Modifier
                                .padding(start = 30.dp, end = 30.dp, bottom = 60.dp)
                        )
                    }
                }
            }
        }
        // ✅ This shows the pull-down loading spinner
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

