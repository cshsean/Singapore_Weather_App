package com.example.weather.ui.background

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import java.time.LocalTime

enum class MOUNTAIN_POSITION {
    BACKGROUND, MIDDLE, FOREGROUND
}

/**
 * Returns a Triple of distinct, opaque colors for the foreground, middle, and background
 * mountain layers based on the current time.
 * The colors transition to simulate atmospheric perspective and time of day.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun getMountainLayerColors(currentTime: LocalTime, currentWeather: String): Triple<Color, Color, Color> {
    val hour = currentTime.hour + currentTime.minute / 60f

    // Base colors
    val nightFg = Color(0xFF0F0515)
    val nightMid = Color(0xFF15081F)
    val nightBg = Color(0xFF1F0F2B)

    val dullNightFg = Color(0xFF0B0310)   // very dark purple-black
    val dullNightMid = Color(0xFF13051B)  // dark purple with slight blue tint
    val dullNightBg = Color(0xFF1A0C26)   // dark purple, but lighter than mid

    val dayFg = Color(0xFF2B5E20)
    val dayMid = Color(0xFF4A7C3F)
    val dayBg = Color(0xFF6B9A60)

    val warmFg = Color(0xFF7A4A00)
    val warmMid = Color(0xFF9E651A)
    val warmBg = Color(0xFFC28033)

    val dullDayFg = Color(0xFF2A352A) // Dark, muted green
    val dullDayMid = Color(0xFF3B4C3B) // Slightly lighter, still deep and subdued
    val dullDayBg = Color(0xFF4D624D) // Lightest, but still quite dark and overcast

    val isRaining = currentWeather.contains("rain", ignoreCase = true)
            || currentWeather.contains("shower", ignoreCase = true)
    val isDaytime = hour in 6f..19.5f

    // If raining during daytime, return misty dull greens
    if (isRaining) {
        return if (isDaytime) {
            Triple(dullDayFg, dullDayMid, dullDayBg)
        } else {
            Triple(dullNightFg, dullNightMid, dullNightBg)
        }
    }

    // Standard time-based color transitions
    return when {
        hour >= 19.5f || hour < 6f -> Triple(nightFg, nightMid, nightBg)
        hour in 6f..7.5f -> {
            val progress = (hour - 6f) / 1.5f
            Triple(
                lerp(nightFg, warmFg, progress),
                lerp(nightMid, warmMid, progress),
                lerp(nightBg, warmBg, progress)
            )
        }
        hour in 7.5f..9f -> {
            val progress = (hour - 7.5f) / 1.5f
            Triple(
                lerp(warmFg, dayFg, progress),
                lerp(warmMid, dayMid, progress),
                lerp(warmBg, dayBg, progress)
            )
        }
        hour in 9f..16.5f -> Triple(dayFg, dayMid, dayBg)
        hour in 16.5f..18f -> {
            val progress = (hour - 16.5f) / 1.5f
            Triple(
                lerp(dayFg, warmFg, progress),
                lerp(dayMid, warmMid, progress),
                lerp(dayBg, warmBg, progress)
            )
        }
        hour in 18f..19.5f -> {
            val progress = (hour - 18f) / 1.5f
            Triple(
                lerp(warmFg, nightFg, progress),
                lerp(warmMid, nightMid, progress),
                lerp(warmBg, nightBg, progress)
            )
        }
        else -> Triple(nightFg, nightMid, nightBg)
    }
}

