package com.example.weather.ui.background

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import java.time.LocalTime
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun rememberCelestialPosition(currentTime: LocalTime): CelestialPosition {
    return remember(currentTime) {
        val hour = currentTime.hour + currentTime.minute / 60f

        val sunriseTime = 6.5f   // 6:30 AM
        val sunsetTime = 18.5f   // 6:30 PM

        val isDayTime = hour in sunriseTime..sunsetTime

        if (isDayTime) {
            val progress = ((hour - sunriseTime) / (sunsetTime - sunriseTime)).coerceIn(0f, 1f)
            CelestialPosition(CelestialBody.SUN, calculateCelestialOffset(progress))
        } else {
            // For the moon, it starts at sunset and ends at next day's sunrise
            val moonProgress = when {
                hour > sunsetTime -> (hour - sunsetTime) / (24 - sunsetTime + sunriseTime)
                else -> (hour + (24 - sunsetTime)) / (24 - sunsetTime + sunriseTime)
            }.coerceIn(0f, 1f)
            CelestialPosition(CelestialBody.MOON, calculateCelestialOffset(moonProgress))
        }
    }
}

internal fun calculateCelestialOffset(progress: Float): Offset {
    val widthScale = 0.6f
    val horizontalOffset = (1f - widthScale) / 2f

    val x = horizontalOffset + progress * widthScale

    val peakHeight = 0.50f   // Max height = 60% of screen (0 is top, 1 is bottom)
    val verticalShift = 0.157f  // Shift everything down by 20% of screen height

    val y = (1f - sin(progress * Math.PI).toFloat()) * peakHeight + verticalShift

    return Offset(x, y)
}

// Data classes
internal data class CelestialPosition(
    val bodyType: CelestialBody,
    val offset: Offset
)

internal enum class CelestialBody {
    SUN, MOON
}