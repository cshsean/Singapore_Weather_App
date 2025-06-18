package com.example.weather.ui.background

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.example.weather.R
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

@Composable
fun RainEffectOverlay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    AsyncImage(
        model = R.drawable.rain_effect, // or R.drawable.<gif>
        imageLoader = imageLoader,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,  // or something else
        alpha = 0.5f
    )
}