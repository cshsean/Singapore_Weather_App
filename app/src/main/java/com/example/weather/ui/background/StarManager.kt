package com.example.weather.ui.background

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset

@Composable
internal fun rememberStars(
    starCount: Int = 100,
    seed: Int = 0
): List<Offset> {
    return remember(seed) {
        val random = kotlin.random.Random(seed)
        List(starCount) {
            Offset(random.nextFloat(), random.nextFloat()) // Normalized coordinates
        }
    }
}