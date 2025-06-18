package com.example.weather.ui.background

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

// --- Cloud Data Structures (remain unchanged) ---
enum class CloudType {
    TYPE1, TYPE2 // Added more types for variety
}

data class Cloud(
    val id: Int,
    val type: CloudType,
    val xOffset: Float,  // Made immutable (val instead of var)
    val startYRatio: Float,
    val scale: Float,     // We'll increase this for bigger cloud
    var currentAlpha: Float = 1.0f
)

// --- Cloud Drawing Logic (drawCloud - MODIFIED PATHS) ---

fun DrawScope.drawCloud(
    cloud: Cloud,
    cloudColor: Color,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val baseCloudPath: Path = when (cloud.type) {
        CloudType.TYPE1 -> Path().apply {
            // Compact cloud with 2 large bumps
            val baseLineY = 90f
            val width = 180f
            val startY = 75f // Adjusted start Y
            val peakY = 40f // Y-coordinate for the highest point of the right curve

            // Start with a rounded C-shape on the left (upwards curve)
            moveTo(0f, startY)
            cubicTo(0f, 55f, 10f, 40f, 35f, 45f) // Left 'C' curve upwards

            // First bump - large
            cubicTo(50f, 20f, 75f, 15f, 90f, 30f)

            // Second bump - large
            cubicTo(105f, 18f, 125f, 22f, 145f, 40f)

            // End with a rounded C-shape on the right (UPWARDS curve)
            cubicTo(155f, 30f, 170f, peakY, width, 45f) // Adjusted for upward curve

            // Bottom segment
            cubicTo(width + 10f, 60f, width + 10f, 75f, width, baseLineY) // Curve from high right point down to baseLineY
            cubicTo(width - 20f, baseLineY, width - 40f, baseLineY, width / 2 + 20f, baseLineY)
            cubicTo(width / 2 - 20f, baseLineY, 40f, baseLineY, 20f, baseLineY)
            cubicTo(0f, baseLineY, 0f, startY, 0f, startY) // Connects to start

            close()
        }

        CloudType.TYPE2 -> Path().apply {
            // Small puffy cloud with 3 bumps
            val baseLineY = 60f
            val width = 140f
            val startY = 45f // Adjusted start Y

            // Start with a rounded C-shape on the left (upwards curve)
            moveTo(0f, startY)

            // First bump - medium
            cubicTo(10f, 15f, 10f, 15f, 40f, 20f)

            // Second bump - large (main bump)
            cubicTo(65f, 8f, 85f, 5f, 100f, 20f)

            // Third bump - small
            cubicTo(110f, 18f, 120f, 22f, 125f, 30f)

            // End with a rounded C-shape on the right (UPWARDS curve)

            // Bottom segment
            cubicTo(width + 5f, 40f, width + 5f, 50f, width, baseLineY) // Curve from high right point down to baseLineY
            cubicTo(width - 15f, baseLineY, width - 30f, baseLineY, width / 2 + 15f, baseLineY)
            cubicTo(width / 2 - 15f, baseLineY, 30f, baseLineY, 15f, baseLineY)
            cubicTo(0f, baseLineY, 0f, startY, 0f, startY) // Connects to start

            close()
        }
    }

    val scaledPath = Path().apply {
        addPath(baseCloudPath)
        val matrix = android.graphics.Matrix().apply {
            setScale(cloud.scale, cloud.scale)
        }
        asAndroidPath().transform(matrix)
    }

    val x = cloud.xOffset * canvasWidth
    val y = cloud.startYRatio * canvasHeight

    translate(left = x, top = y) {
        drawPath(
            path = scaledPath,
            color = cloudColor.copy(alpha = cloudColor.alpha * cloud.currentAlpha)
        )
    }
}

// --- Cloud Color Logic (remains unchanged) ---

@RequiresApi(Build.VERSION_CODES.O)
fun getCloudColor(currentTime: LocalTime): Color {
    val dawnStart = LocalTime.of(5, 0)
    val sunrise = LocalTime.of(6, 30)
    val midMorning = LocalTime.of(9, 0)
    val lateAfternoon = LocalTime.of(17, 0)
    val sunset = LocalTime.of(18, 30)
    val duskEnd = LocalTime.of(20, 0)
    val deepNight = LocalTime.of(22, 0)

    val whiteCloud = Color(0xFFF8F8FF)
    val lightGrayCloud = Color.LightGray.copy(alpha = 0.8f)
    val mediumGrayCloud = Color(0xFFEDEAE0).copy(alpha = 0.7f)
    val darkGrayCloud = Color.DarkGray.copy(alpha = 0.6f)
    val nightPurpleGray = Color(0xFF6A5ACD).copy(alpha = 0.5f)

    return when {
        currentTime.isBefore(dawnStart) -> darkGrayCloud
        currentTime.isBefore(sunrise) -> {
            val progress = ChronoUnit.MINUTES.between(dawnStart, currentTime).toFloat() /
                    ChronoUnit.MINUTES.between(dawnStart, sunrise).toFloat()
            lerp(darkGrayCloud, lightGrayCloud, progress)
        }
        currentTime.isBefore(midMorning) -> {
            val progress = ChronoUnit.MINUTES.between(sunrise, currentTime).toFloat() /
                    ChronoUnit.MINUTES.between(sunrise, midMorning).toFloat()
            lerp(lightGrayCloud, whiteCloud, progress)
        }
        currentTime.isBefore(lateAfternoon) -> whiteCloud
        currentTime.isBefore(sunset) -> {
            val progress = ChronoUnit.MINUTES.between(lateAfternoon, currentTime).toFloat() /
                    ChronoUnit.MINUTES.between(lateAfternoon, sunset).toFloat()
            lerp(whiteCloud, mediumGrayCloud, progress)
        }
        currentTime.isBefore(duskEnd) -> {
            val progress = ChronoUnit.MINUTES.between(sunset, currentTime).toFloat() /
                    ChronoUnit.MINUTES.between(sunset, duskEnd).toFloat()
            lerp(mediumGrayCloud, darkGrayCloud, progress)
        }
        currentTime.isBefore(deepNight) -> darkGrayCloud
        else -> nightPurpleGray
    }
}

// --- Cloud Management Composable (MODIFIED INITIAL DISPERSAL) ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CloudManager(
    currentTime: LocalTime,
    currentWeather: String,
    onCloudsUpdated: (List<Cloud>, Color) -> Unit
) {
    val clouds = remember { mutableStateListOf<Cloud>() }

    val cloudCount = when {
        currentWeather.lowercase().contains("cloudy") && !currentWeather.lowercase().contains("partly") -> 6
        currentWeather.lowercase().contains("partly cloudy") -> 3
        else -> 0
    }

    // STOPPED HERE

    val initialXJitter = 0.1f // Increased jitter range to allow more variation

    val startYRatioList = listOf(0.23f, 0.05f, 0.40f, 0.15f, 0.30f, 0.18f)
    val xOffsetList = listOf(-0.08f, 0.1f, 0.2f, 0.4f, 0.6f, 0.90f)

    LaunchedEffect(Unit) {
        var nextCloudId = 0

        // Create static clouds spread evenly across the screen
        repeat(6) { index ->
            if (cloudCount == 3 && index in listOf(1,3,4)) {
                return@repeat
            }
            if (cloudCount == 0) {
                return@repeat // Skip all if no clouds
            }

            // Divide screen into segments and place cloud in center of segment
            val segmentWidth = 1.0f / cloudCount
            val segmentCenter = index * segmentWidth + (segmentWidth / 2f)

            // Add random jitter that can be both positive and negative
            val randomJitter = ((Random.nextFloat() * initialXJitter * 2) - initialXJitter)*0.9f
            val xOffset = xOffsetList[index] + randomJitter

            // Chance of either cloud type
            val weightedCloudType = listOf(
                CloudType.TYPE1, // 70%
                CloudType.TYPE1,
                CloudType.TYPE1,
                CloudType.TYPE1,
                CloudType.TYPE1,
                CloudType.TYPE1,
                CloudType.TYPE1, // 30%
                CloudType.TYPE2,
                CloudType.TYPE2,
                CloudType.TYPE2
            ).random()

            val newCloud = Cloud(
                id = nextCloudId++,
                type = weightedCloudType,
                xOffset = xOffset.coerceIn(-1.2f, 1f), // Allow clouds to extend slightly beyond screen edges
                startYRatio = startYRatioList[index],
                scale = (0.4f + Random.nextFloat() * 0.5f) + 2f
            )
            clouds.add(newCloud)
        }

        // No animation loop needed since clouds are static
        val cloudColor = getCloudColor(currentTime)
        onCloudsUpdated(clouds, cloudColor)
    }
}