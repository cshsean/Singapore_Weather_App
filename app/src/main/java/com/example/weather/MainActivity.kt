package com.example.weather

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.ui.WeatherApp
import com.example.weather.ui.WeatherAppScreen
import com.example.weather.ui.main.HomeScreen
import com.example.weather.ui.main.MainViewModel
import com.example.weather.ui.theme.WeatherTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel>()

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.composeView.setContent {
            WeatherTheme {
                WeatherApp(
                    onDrawerToggle = {
                        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                            binding.drawerLayout.closeDrawer(GravityCompat.END)
                        } else {
                            binding.drawerLayout.openDrawer(GravityCompat.END)
                        }
                    },
                    mainViewModel = mainViewModel
                )
            }
        }

        // NAVIGATION CLICK HANDLERS
        binding.home.setOnClickListener {
            mainViewModel.navController?.navigate(WeatherAppScreen.HomeScreen.name) {
                popUpTo(WeatherAppScreen.HomeScreen.name) { inclusive = false }
                launchSingleTop = true
            }
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.location.setOnClickListener {
            mainViewModel.navController?.navigate(WeatherAppScreen.SearchLocationScreen.name) {
                popUpTo(WeatherAppScreen.HomeScreen.name) { inclusive = false }
                launchSingleTop = true
            }
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.settings.setOnClickListener {
            mainViewModel.navController?.navigate(WeatherAppScreen.SettingsScreen.name) {
                popUpTo(WeatherAppScreen.HomeScreen.name) { inclusive = false }
                launchSingleTop = true
            }
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }
}

