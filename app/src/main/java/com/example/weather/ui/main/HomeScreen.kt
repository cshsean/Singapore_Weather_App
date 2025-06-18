package com.example.weather.ui.main

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.weather.ui.background.DynamicSkyBackground
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    weatherUiState: WeatherUiState,
    homeScreenViewModel: HomeScreenViewModel,
    settingsScreenViewModel: SettingsScreenViewModel,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    val currentTime by produceState(initialValue = LocalTime.now()) {
        while (true) {
            delay(1000)
            value = LocalTime.now()
        }
    }

    val isDarkMode = settingsScreenViewModel.isDarkMode
    val isImperialUnitEnabled = settingsScreenViewModel.isImperialUnitEnabled

    val isRefreshing = homeScreenViewModel.isRefreshing
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { homeScreenViewModel.refresh() }
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when (weatherUiState) {
            is WeatherUiState.Error -> {
                DynamicSkyBackground(
                    currentTime = currentTime,
                    isDarkMode = isDarkMode
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()), // ✅ Enables pull gesture
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error fetching weather data.\n" +
                                "Turn on Location or recheck your\n" +
                                "internet connection.",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            is WeatherUiState.Loading -> {
                DynamicSkyBackground(
                    currentTime = currentTime,
                    isDarkMode = isDarkMode
                )
                Text(
                    text = "Loading...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            is WeatherUiState.Success -> {
                if (weatherUiState.location == "Outside SG") {
                    DynamicSkyBackground(
                        currentTime = currentTime,
                        isDarkMode = isDarkMode
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "It seems that you are outside of Singapore. " +
                                    "This app only displays weather conditions in Singapore. " +
                                    "If you wish to view them, " +
                                    "use the sidebar to access different locations",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(30.dp)
                                .widthIn(300.dp)
                        )
                    }
                } else {
                    /** DYNAMIC SKY BACKGROUND **/
                    DynamicSkyBackground(
                        currentTime = currentTime,
                        currentWeather = weatherUiState.currentWeather,
                        isDarkMode = isDarkMode
                    )

                    /** RAIN SOUND LOGIC **/
                    val context = LocalContext.current
                    val isRaining = weatherUiState.currentWeather.lowercase().contains("rain") ||
                            weatherUiState.currentWeather.lowercase().contains("shower")
                    val isRainSoundEnabled = settingsScreenViewModel.isRainSoundEnabled

                    // Manage MediaPlayer lifecycle
                    val shouldPlayRain = isRaining && isRainSoundEnabled

                    // Only create and manage MediaPlayer when needed
                    val lifecycleOwner = LocalLifecycleOwner.current

                    DisposableEffect(shouldPlayRain, lifecycleOwner) {
                        var mediaPlayer: MediaPlayer? = null

                        if (shouldPlayRain) {
                            mediaPlayer = MediaPlayer.create(context, R.raw.rain_sound).apply {
                                isLooping = true
                                start()
                            }
                        }

                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> {
                                    if (shouldPlayRain) mediaPlayer?.start()
                                }
                                Lifecycle.Event.ON_PAUSE,
                                Lifecycle.Event.ON_STOP -> {
                                    mediaPlayer?.pause()
                                }
                                Lifecycle.Event.ON_DESTROY -> {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                }
                                else -> Unit
                            }
                        }

                        lifecycleOwner.lifecycle.addObserver(observer)

                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .verticalScroll(rememberScrollState())
                    ) {

                        Spacer(modifier = Modifier.height(175.dp))

                        // === TOP SECTION: LOCATION & TEMPERATURE ===
                        if (weatherUiState.location.isNotEmpty()) {
                            Text(
                                text = weatherUiState.location,
                                textAlign = TextAlign.Center,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.W300,
                                style = TextStyle(
                                    lineHeight = 35.sp,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                color = Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 50.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy((-12).dp)
                            ) {
                                val temperature = if (!isImperialUnitEnabled) {
                                    weatherUiState.temperature
                                } else {
                                    convertToImperial(weatherUiState.temperature, "Temperature")
                                }

                                Text(
                                    text = temperature.toString(),
                                    fontSize = 100.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.2f),
                                            offset = Offset(2f, 2f),
                                            blurRadius = 4f
                                        )
                                    ),
                                    letterSpacing = (-1.0).sp,
                                    color = Color.White
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
                                text = weatherUiState.currentWeather,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.W300,
                                color = Color.White,
                                style = TextStyle(
                                    lineHeight = 25.sp,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(
                                        bottom = 100.dp,
                                        top = 0.dp,
                                        start = 50.dp,
                                        end = 50.dp
                                    )
                            )
                        }

                        // === UV INDEX SLIDER ===
                        val uvValue = weatherUiState.UVIndex.toFloat()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UVIndexSlider(
                                value = uvValue,
                                modifier = Modifier.widthIn(max = 400.dp)
                            )
                        }

                        // === 24-HOUR FORECAST ===
                        val forecastItems = if (weatherUiState.hourlyForecast.isNotEmpty())
                            weatherUiState.hourlyForecast
                        else emptyList()

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 25.dp)
                        ) {
                            Text(
                                text = "24 Hour Forecast",
                                color = Color.White,
                                fontSize = 16.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                LazyRow(
                                    modifier = Modifier
                                        .widthIn(max = 1000.dp)
                                        .padding(horizontal = 25.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(forecastItems) { item ->
                                        HourlyForecastCard(
                                            time = item.time,
                                            weatherStatus = item.weatherStatus
                                        )
                                    }
                                }
                            }
                        }

                        // WIND AND RAINFALL
                        WindAndRainRow(
                            windSpeed = weatherUiState.windSpeed,
                            rainfall = weatherUiState.rainfall,
                            isImperialUnitEnabled = isImperialUnitEnabled,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = "credits for weather icon and rain animation\n" +
                                    "goes to iconixar and douglas schatz respectively",
                            fontSize = 10.sp,
                            color = Color.White,
                            style = TextStyle(
                                lineHeight = 15.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(
                                    start = 30.dp,
                                    end = 30.dp,
                                    bottom = 60.dp
                                )
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun UVIndexSlider(
    value: Float,
    modifier: Modifier = Modifier
) {
    val sliderHeight = 6.dp
    val thumbRadius = 7.dp
    val sidePadding = 45.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = sidePadding)
                .fillMaxWidth()
        ) {
            Text(
                text = "UV Index",
                color = Color.White,
                fontSize = 15.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
            Text(
                text = "$value",
                color = Color.White,
                fontSize = 15.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sidePadding)
        ) {
            val availableWidth = maxWidth - thumbRadius * 2
            val clampedValue = (value / 10f).coerceIn(0f, 1f)
            val thumbOffset = availableWidth * clampedValue

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbRadius * 2),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(sliderHeight)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF4CAF50), Color.Yellow, Color.Red)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .size(thumbRadius * 2)
                        .background(Color.White, shape = CircleShape)
                        .border(1.dp, Color.Gray, shape = CircleShape)
                )
            }
        }
    }
}

@Composable
fun HourlyForecastCard(
    time: String,
    weatherStatus: String,
    modifier: Modifier = Modifier
) {
    val imageRes = getWeatherImageRes(weatherStatus)

    Card(
        modifier = modifier
            .height(150.dp)
            .width(90.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f) // Translucent white
        )
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent) // Ensure column doesn't override card's alpha
                .padding(vertical = 25.dp)
        ) {
            Text(
                text = time,
                color = Color.White,
                fontSize = 17.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = weatherStatus,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun WindAndRainRow(
    windSpeed: Float,          // in km/h
    rainfall: Double,         // in mm
    isImperialUnitEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(top = 5.dp, start = 16.dp, end = 16.dp, bottom = 30.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val windSpeed = if (!isImperialUnitEnabled) {
            Pair<Float, String>(windSpeed, "km/h")
        } else {
            Pair<Double, String>(convertToImperial(windSpeed.toDouble(), "Wind Speed"), "m/h")
        }

        val rainfall = if (!isImperialUnitEnabled) {
            Pair<Double, String>(rainfall, "mm")
        } else {
            Pair<Double, String>(convertToImperial(rainfall, "Rainfall"), "in³")
        }

        InfoCard(
            icon = Icons.Default.Air,
            label = "Wind Speed",
            value = "${windSpeed.first} ${windSpeed.second}",
            modifier = Modifier
                .height(150.dp)
                .widthIn(max = 175.dp)
        )
        InfoCard(
            icon = Icons.Default.InvertColors,
            label = "Rainfall",
            value = "${rainfall.first} ${rainfall.second}",
            modifier = Modifier
                .height(150.dp)
                .widthIn(max = 175.dp)
        )
    }
}


@Composable
fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



fun getWeatherImageRes(weatherStatus: String): Int {
    val status = weatherStatus.lowercase()

    return when {
        "fair" in status && "day" in status -> R.drawable.fair_day
        "fair" in status && "night" in status -> R.drawable.fair_night
        "partly cloudy" in status && "day" in status -> R.drawable.cloudy_day
        "partly cloudy" in status && "night" in status -> R.drawable.cloudy_night
        "cloudy" in status -> R.drawable.cloudy_day
        "light rain" in status || "showers" in status -> R.drawable.light_rain
        "moderate rain" in status || "heavy rain" in status -> R.drawable.heavy_rain
        "thundery" in status -> R.drawable.lightning_rain
        else -> R.drawable.cloudy_day
    }
}

fun convertToImperial(metric: Double, type: String): Double {
    val result = when (type) {
        "Temperature" -> (metric * 9 / 5) + 32
        "Rainfall" -> metric * 0.03937
        "Wind Speed" -> metric * 0.621371
        else -> throw Error()
    }
    return String.format(Locale.US,"%.1f", result).toDouble()
}

/**
 * REMAINING THINGS TO DO:
 * 1) ADD WIND SPEED AND CHANCE OF RAIN
 * 2) ADD CLOUDS/EFFECT IN BACKGROUND, DEPENDING ON WEATHER
 * 3) ADD ANIMATED LOADING/ERROR SCREEN
 * **/
