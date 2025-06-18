package com.example.weather.ui.background

import com.example.weather.R
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat.Surface
import com.example.weather.ui.main.SettingsScreenViewModel
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DynamicSkyBackground(
    currentTime: LocalTime,
    currentWeather: String = "Fair",
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (isDarkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
        )
    } else {

        val gradient = getSkyGradient(currentTime, currentWeather)
        val celestialPosition = rememberCelestialPosition(currentTime)

        var currentClouds by remember { mutableStateOf(emptyList<Cloud>()) }
        var currentCloudColor by remember { mutableStateOf(Color.White) }

        var isRaining = currentWeather.lowercase().contains("rain") ||
                currentWeather.lowercase().contains("shower")
        var isLightning = currentWeather.lowercase().contains("thunder")

        CloudManager(
            currentTime = currentTime,
            currentWeather = currentWeather
        ) { clouds, cloudColor ->
            currentClouds = clouds
            currentCloudColor = cloudColor
        }

        val mountainHeightForegroundPx = with(LocalDensity.current) { 170.dp.toPx() }
        val mountainHeightMiddlePx = with(LocalDensity.current) { 240.dp.toPx() }
        val mountainHeightBackgroundPx = with(LocalDensity.current) { 340.dp.toPx() }

        // Lightning state
        var lightningVisible by remember { mutableStateOf(false) }

        // Stars
        val hour = currentTime.hour + currentTime.minute / 60f
        val isNight = hour < 6.0f || hour > 19.5f

        val stars = rememberStars(20) // 150 stars for nice density

        // Launch coroutine to randomly trigger lightning during rain
        LaunchedEffect(isLightning) {
            if (isLightning) {
                while (true) {
                    // Wait random delay between bursts (5-15s)
                    delay((2000L..8000L).random())
                    // Number of flashes per burst (2-4 flashes)
                    val flashesInBurst = (2..4).random()
                    repeat(flashesInBurst) {
                        lightningVisible = true
                        delay((50L..150L).random()) // flash duration, short & variable
                        lightningVisible = false
                        delay((50L..200L).random()) // pause between flashes in burst
                    }
                }
            } else {
                // Reset lightning when not raining
                lightningVisible = false
            }
        }

        Box(modifier = modifier) {
            // 🎨 Main background Canvas (sky, clouds, celestial, mountains)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                drawRect(brush = gradient)
                drawCelestialBodies(celestialPosition, size, currentTime, currentWeather)

                currentClouds.forEach { cloud ->
                    drawCloud(cloud, currentCloudColor, canvasWidth, canvasHeight)
                }

                if (isNight) {
                    val starColor = Color.White.copy(alpha = 0.8f)
                    val starRadius = 1.5f

                    stars.forEach { star ->
                        val x = star.x * size.width
                        val y = star.y * size.height
                        drawCircle(color = starColor, center = Offset(x, y), radius = starRadius)
                    }
                }

                val (foregroundColor, middleColor, backgroundColor) = getMountainLayerColors(
                    currentTime,
                    currentWeather
                )

                drawMountainLayer(
                    canvasWidth, canvasHeight, mountainHeightBackgroundPx,
                    position = MOUNTAIN_POSITION.BACKGROUND,
                    tint = backgroundColor
                )
                drawMountainLayer(
                    canvasWidth, canvasHeight, mountainHeightMiddlePx,
                    position = MOUNTAIN_POSITION.MIDDLE,
                    tint = middleColor
                )
                drawMountainLayer(
                    canvasWidth, canvasHeight, mountainHeightForegroundPx,
                    position = MOUNTAIN_POSITION.FOREGROUND,
                    tint = foregroundColor
                )
            }

            // 🌧️ Overlay animated rain *on top* of canvas background
            if (isRaining) {
                RainEffectOverlay(modifier = Modifier.matchParentSize())
            }

            // ⚡ Lightning flash effect - dull, inset around screen edges
            if (lightningVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Use a dull white with low alpha
                        .background(Color.White.copy(alpha = 0.15f))
                )
            }
        }
    }
}

private fun DrawScope.drawMountainLayer(
    canvasWidth: Float,    // Full width of the main canvas
    canvasHeight: Float,   // Full height of the main canvas
    mountainDrawingHeightPx: Float, // The conceptual "height" of this specific mountain layer's drawing area
    position: MOUNTAIN_POSITION,
    tint: Color
) {
    val pathWidth = canvasWidth // Mountains span the full width of the main canvas

    // baseY is calculated as 10% from the "top" of this mountain layer's conceptual drawing area (mountainDrawingHeightPx)
    // This means the highest peaks will be relative to this baseY.
    val baseY = mountainDrawingHeightPx * 0.1f

    val path = Path().apply {
        // Start at bottom-left of the conceptual drawing area for this layer.
        // The entire path will be translated later to align its bottom with the main canvas's bottom.
        moveTo(0f, mountainDrawingHeightPx)

        when (position) {
            MOUNTAIN_POSITION.BACKGROUND -> { // Tallest peak profile
                lineTo(0f, baseY + 50f)
                lineTo(pathWidth * 0.05f, baseY + 10f)
                lineTo(pathWidth * 0.12f, baseY - 30f)
                lineTo(pathWidth * 0.2f, baseY - 10f)
                lineTo(pathWidth * 0.35f, baseY - 90f)
                lineTo(pathWidth * 0.43f, baseY - 140f) // tallest peak for this profile
                lineTo(pathWidth * 0.52f, baseY - 120f)
                lineTo(pathWidth * 0.58f, baseY - 90f)
                lineTo(pathWidth * 0.70f, baseY - 70f)
                lineTo(pathWidth * 0.78f, baseY - 20f)
                lineTo(pathWidth * 0.85f, baseY - 15f)
                lineTo(pathWidth * 0.93f, baseY + 20f)
                lineTo(pathWidth, baseY + 60f)
                lineTo(pathWidth, mountainDrawingHeightPx) // End at bottom-right
            }
            MOUNTAIN_POSITION.MIDDLE -> { // Gentle rolling peaks profile
                lineTo(0f, baseY + 20f)
                lineTo(pathWidth * 0.1f, baseY + 20f)
                lineTo(pathWidth * 0.2f, baseY - 38f)
                lineTo(pathWidth * 0.35f, baseY - 10f)
                lineTo(pathWidth * 0.6f, baseY - 30f)
                lineTo(pathWidth * 0.7f, baseY - 50f)
                lineTo(pathWidth * 0.85f, baseY + 10f)
                lineTo(pathWidth, baseY + 40f)
                lineTo(pathWidth, mountainDrawingHeightPx)
            }
            MOUNTAIN_POSITION.FOREGROUND -> { // Subtle hills profile
                lineTo(0f, baseY + 40f)
                lineTo(pathWidth * 0.15f, baseY - 20f)
                lineTo(pathWidth * 0.3f, baseY + 5f)
                lineTo(pathWidth * 0.45f, baseY - 30f)
                lineTo(pathWidth * 0.5f, baseY - 28f)
                lineTo(pathWidth * 0.6f, baseY - 10f)
                lineTo(pathWidth * 0.75f, baseY - 25f)
                lineTo(pathWidth * 0.95f, baseY + 15f)
                lineTo(pathWidth, baseY + 40f) // Matched original end Y for consistency
                lineTo(pathWidth, mountainDrawingHeightPx)
            }
        }
        close()
    }

    // Translate the drawing context.
    // The path is defined as if its bottom is at y=mountainDrawingHeightPx (within its conceptual height).
    // We want to draw this path such that its bottom aligns with y=canvasHeight (the bottom of the main canvas).
    // So, we shift the drawing origin upwards by (canvasHeight - mountainDrawingHeightPx).
    translate(left = 0f, top = canvasHeight - mountainDrawingHeightPx) {
        drawPath(
            path = path,
            color = tint // Use the provided opaque tint
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun DrawScope.drawCelestialBodies(
    position: CelestialPosition,
    canvasSize: Size,
    currentTime: LocalTime, // Add current time parameter
    currentWeather: String // <-- Add this
) {
    if (currentWeather.contains("rain", ignoreCase = true)
        || currentWeather.contains("shower", ignoreCase = true)) return

    val (bodyType, offset) = position
    val center = Offset(canvasSize.width * offset.x, canvasSize.height * offset.y)

    val coreRadius = 100f
    val glowRadius1 = 350f
    val glowRadius2 = 220f

    when (bodyType) {
        CelestialBody.SUN -> {
            // Define time points
            val sunrise = LocalTime.of(7, 30)
            val morningTransitionEnd = LocalTime.of(9, 0)
            val eveningTransitionStart = LocalTime.of(17, 0)
            val sunset = LocalTime.of(19, 0)

            // Calculate color based on time
            val (coreColor, innerGlowColor, outerGlowColor) = when {
                currentTime.isBefore(sunrise) || currentTime.isAfter(sunset) -> {
                    // Night time - shouldn't normally be called, but fallback
                    Triple(Color(0xFFFFA000), Color(0xFFFFC107), Color(0xFFFFF176))
                }
                currentTime.isBefore(morningTransitionEnd) -> {
                    // Sunrise to 9 AM - transition from orange to yellow
                    val progress = ChronoUnit.MINUTES.between(sunrise, currentTime).toFloat() /
                            ChronoUnit.MINUTES.between(sunrise, morningTransitionEnd).toFloat()
                    val interpolatedCore = lerp(
                        Color(0xFFFFA000), // Deep orange
                        Color(0xFFFFFF9D), // Pastel yellow
                        progress
                    )
                    val interpolatedInner = lerp(
                        Color(0xFFFFC107), // Amber
                        Color(0xFFFFFFC8), // Light yellow
                        progress
                    )
                    val interpolatedOuter = lerp(
                        Color(0xFFFFF176), // Orange-yellow
                        Color(0xFFFFFFE0), // Very light yellow
                        progress
                    )
                    Triple(interpolatedCore, interpolatedInner, interpolatedOuter)
                }
                currentTime.isBefore(eveningTransitionStart) -> {
                    // 9 AM to 6 PM - bright pastel yellow
                    Triple(
                        Color(0xFFFFFF9D), // Pastel yellow core
                        Color(0xFFFFFFC8), // Light yellow inner glow
                        Color(0xFFFFFFE0)  // Very light yellow outer glow
                    )
                }
                currentTime.isBefore(sunset) -> {
                    // 6 PM to sunset - transition back to orange
                    val progress = ChronoUnit.MINUTES.between(eveningTransitionStart, currentTime).toFloat() /
                            ChronoUnit.MINUTES.between(eveningTransitionStart, sunset).toFloat()
                    val interpolatedCore = lerp(
                        Color(0xFFFFFF9D), // Pastel yellow
                        Color(0xFFFFA000), // Deep orange
                        progress
                    )
                    val interpolatedInner = lerp(
                        Color(0xFFFFFFC8), // Light yellow
                        Color(0xFFFFC107), // Amber
                        progress
                    )
                    val interpolatedOuter = lerp(
                        Color(0xFFFFF176), // Very light yellow
                        Color(0xFFFFF176), // Orange-yellow
                        progress
                    )
                    Triple(interpolatedCore, interpolatedInner, interpolatedOuter)
                }
                else -> {
                    // Shouldn't reach here
                    Triple(Color(0xFFFFA000), Color(0xFFFFC107), Color(0xFFFFF176))
                }
            }

            // Draw with time-appropriate colors
            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(outerGlowColor.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = glowRadius1
                ),
                center = center,
                radius = glowRadius1
            )

            // Inner glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(innerGlowColor.copy(alpha = 0.7f), Color.Transparent),
                    center = center,
                    radius = glowRadius2
                ),
                center = center,
                radius = glowRadius2
            )

            // Core sun
            drawCircle(
                color = coreColor,
                center = center,
                radius = coreRadius
            )
        }

        CelestialBody.MOON -> {
            // Moon remains unchanged
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.LightGray.copy(alpha = 0.3f), Color.Transparent),
                    center = center,
                    radius = 180f
                ),
                center = center,
                radius = 180f
            )

            drawCircle(
                color = Color.LightGray,
                center = center,
                radius = coreRadius
            )
        }
    }
}

// Helper classes and function
data class TimeGradientPoint(val time: Float, val topColor: Color, val bottomColor: Color)

data class GradientSegment(
    val startTime: Float,
    val startTop: Color,
    val startBottom: Color,
    val endTime: Float,
    val endTop: Color,
    val endBottom: Color
)

