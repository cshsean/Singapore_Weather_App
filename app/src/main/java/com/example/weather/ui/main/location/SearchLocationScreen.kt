package com.example.weather.ui.main.location

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.weather.data.LocationEntity
import com.example.weather.ui.WeatherAppScreen
import com.example.weather.ui.background.DynamicSkyBackground
import com.example.weather.ui.main.SettingsScreenViewModel
import com.example.weather.ui.main.WeatherUiState
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SearchLocationScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    weatherUiState: WeatherUiState,
    navController: NavController,
    searchLocationViewModel: SearchLocationViewModel,
    settingsScreenViewModel: SettingsScreenViewModel
) {
    var locationList = searchLocationViewModel.searchLocations().collectAsState(initial = emptyList())
    var favoriteList = searchLocationViewModel.getFavorites().collectAsState(initial = emptyList())

    val isDarkMode = settingsScreenViewModel.isDarkMode

    if (weatherUiState is WeatherUiState.Error) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            DynamicSkyBackground(
                currentTime = LocalTime.now(),
                isDarkMode = isDarkMode
            )
            Text(
                text = "Error fetching weather data.\n" +
                        "Turn on Location or recheck your\n" +
                        "internet connection.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    } else {
        Surface(
            color = if (isDarkMode) Color.Black else Color(0xFF2A3D66),
            modifier = modifier
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(vertical = 42.dp))
                WeatherSearchBar(viewModel = searchLocationViewModel)
                Spacer(modifier = Modifier.padding(vertical = 6.dp))
                when{
                    searchLocationViewModel.retrieveSearchText().isEmpty() -> FavoriteScreen(
                        favoriteList = favoriteList.value,
                        onItemClick = { location ->
                            val locationJson = Uri.encode(Json.encodeToString(location))
                            navController.navigate("${WeatherAppScreen.MockLocationScreen.name}?location=$locationJson")
                        },
                        viewModel = searchLocationViewModel
                    )

                    else -> ResultScreen(
                        locationList = locationList.value,
                        onItemClick = { location ->
                            val locationJson = Uri.encode(Json.encodeToString(location))
                            navController.navigate("${WeatherAppScreen.MockLocationScreen.name}?location=$locationJson")
                        },
                        viewModel = searchLocationViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherSearchBar(
    viewModel: SearchLocationViewModel,
    modifier: Modifier = Modifier
) {
    val textColor = if (isSystemInDarkTheme()) Color.Black else Color.Black

    TextField(
        value = viewModel.retrieveSearchText(),
        onValueChange = { viewModel.updateSearchText(it) },
        shape = RoundedCornerShape(100.dp),
        placeholder = {
            Text(
                text = "Search For Location",
                fontSize = 18.sp
            )
        },
        leadingIcon = {
            IconButton(onClick = {/*TODO*/}) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "search",
                    tint = Color.Black,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(28.dp)
                )
            }
        },
        maxLines = 1,
        textStyle = TextStyle(
            color = textColor,
            fontSize = 18.sp
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF0F0F0),
            unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color.Black
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),
    )

    // Testing to ensure viewmodel and screen are connected
    LaunchedEffect(viewModel.retrieveSearchText()) {
        Log.d("SearchText", "Current query: ${viewModel.retrieveSearchText()}")
    }
}


