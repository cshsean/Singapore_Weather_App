package com.example.weather.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.weather.WeatherTopAppBar
import com.example.weather.data.LocationEntity
import com.example.weather.ui.background.DynamicSkyBackground
import com.example.weather.ui.main.HomeScreen
import com.example.weather.ui.main.HomeScreenViewModel
import com.example.weather.ui.main.MainViewModel
import com.example.weather.ui.main.location.SearchLocationScreen
import com.example.weather.ui.main.SettingsScreen
import com.example.weather.ui.main.SettingsScreenViewModel
import com.example.weather.ui.main.location.MockLocationScreen
import com.example.weather.ui.main.location.SearchLocationViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.time.LocalTime

enum class WeatherAppScreen {
    HomeScreen,
    SearchLocationScreen,
    SettingsScreen,
    MockLocationScreen
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp(
    modifier: Modifier = Modifier,
    onDrawerToggle: () -> Unit,
    mainViewModel: MainViewModel
) {
    val homeScreenViewModel: HomeScreenViewModel = viewModel(factory = HomeScreenViewModel.Factory)
    val searchLocationViewModel: SearchLocationViewModel = viewModel(factory = SearchLocationViewModel.Factory)

    val context = LocalContext.current
    val settingsScreenViewModel: SettingsScreenViewModel = viewModel(
        factory = SettingsScreenViewModel.Factory(context)
    )

    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        mainViewModel.navController = navController
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            // We get the current route here using navController.currentBackStackEntryAsState()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Show top app bar on all routes except MockLocationScreen
            if (currentRoute?.startsWith(WeatherAppScreen.MockLocationScreen.name) == false) {
                WeatherTopAppBar(
                    onMenuClick = onDrawerToggle,
                    currentRoute = currentRoute, // <-- Pass it here
                    scrollBehavior = if (currentRoute == WeatherAppScreen.HomeScreen.name) scrollBehavior else null
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WeatherAppScreen.HomeScreen.name,
        ) {
            composable(route = WeatherAppScreen.HomeScreen.name) {
                Box(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        weatherUiState = homeScreenViewModel.weatherUiState,
                        homeScreenViewModel = homeScreenViewModel,
                        settingsScreenViewModel = settingsScreenViewModel,
                        scrollBehavior = scrollBehavior,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            composable(route = WeatherAppScreen.SearchLocationScreen.name) {
                SearchLocationScreen(
                    navController = navController,
                    weatherUiState = homeScreenViewModel.weatherUiState,
                    settingsScreenViewModel = settingsScreenViewModel,
                    searchLocationViewModel = searchLocationViewModel,
                    paddingValues = innerPadding
                )
            }
            composable(route = WeatherAppScreen.SettingsScreen.name) {
                SettingsScreen(
                    viewModel = settingsScreenViewModel,
                    homeScreenViewModel = homeScreenViewModel
                )
            }
            composable(
                route = "${WeatherAppScreen.MockLocationScreen.name}?location={location}",
                arguments = listOf(navArgument("location") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val locationJson = backStackEntry.arguments?.getString("location")
                val location = locationJson?.let { Json.decodeFromString<LocationEntity>(it) }
                MockLocationScreen(
                    location = location,
                    navController = navController,
                    viewModel = searchLocationViewModel,
                    settingsScreenViewModel = settingsScreenViewModel
                )
            }
        }
    }
}




