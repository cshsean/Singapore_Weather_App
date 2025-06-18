package com.example.weather.ui.background

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
fun getSkyGradient(currentTime: LocalTime, currentWeather: String): Brush {
    val isRaining = currentWeather.contains("rain", ignoreCase = true)

    val hour = currentTime.hour + currentTime.minute / 60f
    val t = hour / 24f

    // Define colors
    val darkPurpleTop = Color(0xFF2A0A5E)     // Rich dark purple
    val darkPurpleBottom = Color(0xFF1B003B)  // Deeper, almost black-purple

    val orangeTop = Color(0xFFFFB74D)
    val orangeBottom = Color(0xFFFF8A65)

    val blueTop = Color(0xFF64B5F6)
    val blueBottom = Color(0xFF0D47A1)

    val rainyNightTop = Color(0xFF17151D)    // even darker purple-grey
    val rainyNightBottom = Color(0xFF1F1B27) // deep muted plum, very low brightness

    // Define time-color points (in order)
    val segments = listOf(
        TimeGradientPoint(6f / 24f, darkPurpleTop, darkPurpleBottom),   // 6:00 AM
        TimeGradientPoint(7.5f / 24f, orangeTop, orangeBottom),         // 7:30 AM
        TimeGradientPoint(9f / 24f, blueTop, blueBottom),               // 9:00 AM
        TimeGradientPoint(16.5f / 24f, blueTop, blueBottom),              // 4:00 PM
        TimeGradientPoint(18f / 24f, orangeTop, orangeBottom),          // 6:00 PM
        TimeGradientPoint(19.5f / 24f, darkPurpleTop, darkPurpleBottom)   // 8:00 PM
    )

    val segment: GradientSegment = run {
        for (i in 0 until segments.size - 1) {
            val (startT, sTop, sBottom) = segments[i]
            val (endT, eTop, eBottom) = segments[i + 1]
            if (t in startT..endT) {
                return@run GradientSegment(startT, sTop, sBottom, endT, eTop, eBottom)
            }
        }

        // Outside of defined segments — use the nearest fixed color
        return@run if (t < segments.first().time) {
            GradientSegment(0f, darkPurpleTop, darkPurpleBottom, 0f, darkPurpleTop, darkPurpleBottom)
        } else {
            GradientSegment(1f, darkPurpleTop, darkPurpleBottom, 1f, darkPurpleTop, darkPurpleBottom)
        }
    }

    val segmentProgress = if (segment.startTime != segment.endTime) {
        ((t - segment.startTime) / (segment.endTime - segment.startTime)).coerceIn(0f, 1f)
    } else 0f

    val gradientColors = listOf(
        lerp(segment.startTop, segment.endTop, segmentProgress),
        lerp(segment.startBottom, segment.endBottom, segmentProgress)
    )

    val rainyTop = Color(0xFF6D88A0)
    val rainyBottom = Color(0xFF3F5B70)

    val darken = if (isRaining) 0.65f else 1.0f

    val isNight = t < (6f / 24f) || t > (19.5f / 24f)

    val finalGradient = when {
        isRaining && isNight -> listOf(rainyNightTop, rainyNightBottom) // no alpha darkening
        isRaining -> listOf(
            lerp(gradientColors[0], rainyTop, 0.35f),
            lerp(gradientColors[1], rainyBottom, 0.35f)
        ).map { it.copy(alpha = it.alpha * darken) } // apply darken here
        else -> gradientColors
    }

    return Brush.verticalGradient(colors = finalGradient)
}