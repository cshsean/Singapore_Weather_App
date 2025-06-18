plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // FOR ROOM
    alias(libs.plugins.ksp)
    // IMPORTANT FOR SERIALIZATION
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.example.weather"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weather"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

val room_version = "2.7.1"
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Retrofit with Scalar Converter
    // Removed: implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Lottie for Compose
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Location Service
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Additional Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Room
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Jetpack Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // Drawer Layout
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Gson to parse JSON
    implementation("com.google.code.gson:gson:2.11.0")

    // GIF Animation
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    // Pull to Refresh
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material:1.8.2")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")

    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.1.7")
}