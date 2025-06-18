package com.example.weather.ui.main

import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weather.R
import com.example.weather.ui.background.DynamicSkyBackground
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
    homeScreenViewModel: HomeScreenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val weatherUiState = homeScreenViewModel.weatherUiState

    val isRaining = remember(weatherUiState) {
        if (weatherUiState is WeatherUiState.Success) {
            val hasRainKeyword = weatherUiState.currentWeather.contains("rain", ignoreCase = true) ||
                    weatherUiState.currentWeather.contains("shower", ignoreCase = true)
            val hasRainfall = weatherUiState.rainfall > 0.1
            hasRainKeyword || hasRainfall
        } else {
            false
        }
    }

    val isRainSoundEnabled = viewModel.isRainSoundEnabled
    val shouldPlayRain = isRaining && isRainSoundEnabled

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
    val useImperialUnits = viewModel.isImperialUnitEnabled
    val isDarkMode = viewModel.isDarkMode

    Box(modifier = modifier.fillMaxSize()) {
        DynamicSkyBackground(
            currentTime = LocalTime.now(),
            isDarkMode = isDarkMode
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            // 📦 App Defaults Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Defaults",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggle("Rain Sound", isRainSoundEnabled) {
                        viewModel.toggleRainSound()
                    }
                    SettingToggle("Use Imperial Units", useImperialUnits) {
                        viewModel.toggleImperialUnit()
                    }
                    SettingToggle("Dark Mode", isDarkMode) {
                        viewModel.toggleDarkMode()
                    }
                }
            }

            // 📝 Credits Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Credits & Acknowledgements",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val credits = listOf(
                        "Weather data: National Environmental Agency (NEA)",
                        "Rain sound: Liecio (Pixabay)",
                        "Rain animation: Douglas Schatz (Giphy)",
                        "Icons: Iconixar (Flaticon)",
                        "Built with Jetpack Compose",
                        "Created by cshsean"
                    )

                    credits.forEach {
                        Text("• $it", fontSize = 14.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "© 2025 SG Weather App",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingToggle(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}
