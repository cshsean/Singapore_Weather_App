package com.example.weather

import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.weather.ui.WeatherAppScreen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopAppBar(
    onMenuClick: () -> Unit,
    currentRoute: String?, // <-- Add this
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    val currentDate = remember { LocalDate.now() }
    val formattedDate = remember {
        currentDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
    }

    TopAppBar(
        title = {
            val topAppBarTitle = when {
                currentRoute == WeatherAppScreen.SettingsScreen.name -> "Settings"
                currentRoute != WeatherAppScreen.SettingsScreen.name -> formattedDate.toString()
                else -> ""
            }

            Text(
                text = topAppBarTitle,
                color = Color.White,
                fontSize = if (topAppBarTitle == "Settings") 30.sp else 24.sp,
                fontWeight = if (topAppBarTitle == "Settings") FontWeight.Bold else FontWeight.Normal,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.offset(y = (-4).dp)
            )
        },
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    tint = Color.White,
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-4).dp, y = (-5).dp)
                        .size(34.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            actionIconContentColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        modifier = modifier
    )
}
