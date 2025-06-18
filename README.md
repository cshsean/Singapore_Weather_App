# ☀️ SG Weather App — A Weather App for Singapore

SGWeather is a modern Android weather application designed specifically for Singapore. It provides real-time local weather information, forecasts, and a smooth user experience with dynamic visuals and helpful features.

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/f289b4b5-8621-495e-aa2c-bbd155c78ae9" alt="weather_home" width="200"/>
  <img src="https://github.com/user-attachments/assets/e15f7711-9005-40fc-aa9a-28c049acc05f" alt="weather_home_dawn" width="200"/>
  <img src="https://github.com/user-attachments/assets/e2b7db8e-539a-4cdf-b702-8b8e7612c11c" alt="weather_home_night" width="200"/>
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/b5e9f569-d2de-4a28-b2bc-891746636e35" alt="weather_fav" width="200"/>
  <img src="https://github.com/user-attachments/assets/8f7cc03d-1200-411b-a94f-00c4b0c8b71c" alt="weather_mock" width="200"/>
  <img src="https://github.com/user-attachments/assets/0908e789-9eb0-4a78-bba7-fd33d4646650" alt="weather_settings" width="200"/>
</p>

## 🧭 Features

- 🌦️ **Current Weather**: Displays real-time temperature, rainfall, wind speed, UV index, and weather condition based on your location.
- 🎨 **Dynamic Background**: Weather-based animated sky that changes according to the current weather status (e.g. cloudy, rainy, clear).
- 🕒 **24-Hour Forecast**: Shows a simplified 24-hour regional forecast, updated in 6-hour intervals.
- 🔎 **Location Search**: Search for any place in Singapore and view its current weather.
- ⭐ **Favorites System**:
  - Tap on a location to view detailed weather data.
  - Add/remove locations from your favorites list.
  - Favorites appear when the search bar is empty.
- ⚙️ **Settings**:
  - Toggle between metric and imperial units.
  - Toggle dark mode.
  - Enable or disable ambient rain sound effects when it’s raining.

---


## 🔧 How It Works

### 📡 Weather Data
- Data is fetched from Singapore’s [data.gov.sg](https://data.gov.sg) APIs:
  - **2-Hour Forecast**
  - **24-Hour Forecast**
  - **Temperature**, **Rainfall**, **Wind Speed**, and **UV Index**

### 📍 Location Handling
- Uses the device's current GPS location.
- Determines the nearest forecast area using **Haversine distance**.
- Maps the area to a regional forecast zone (north, south, central, east, west).

### 🧠 ViewModels & UI State
- Built with **Jetpack Compose** for modern declarative UI.
- MVVM architecture with `HomeScreenViewModel` and `SearchLocationViewModel`.
- Each screen observes a sealed UI state: `Loading`, `Success`, or `Error`.

### 🎨 Design & UX
- Features custom weather-themed animated backgrounds.
- Composable-based design system with a responsive layout.
- Favorites system and persistent settings (e.g. dark mode, rain sounds) via **Jetpack DataStore**.

---

## 🧠 What I Learned

As my first complete Android project, I’ve learned a lot:

- 📱 **Jetpack Compose**: Building declarative UI and managing UI state reactively.
- 🏗️ **MVVM Architecture**: Separating concerns and making the app scalable.
- 🌐 **Networking & APIs**: Handling HTTP requests, parsing JSON, and working with real-time data.
- 🧭 **Location Services**: Getting device location and safely handling permission flow.
- 📊 **UI State Management**: Using `StateFlow` and `mutableStateOf` effectively.
- 🎨 **Design Thinking**: Creating a user-friendly interface that adapts to weather conditions.
- 🎚️ **Settings & Persistence**: Using Jetpack DataStore for user preferences.

---

## 🔍 Areas of Improvement

Though the app is functional, there are several areas I could enhance:

- 📶 **Error Handling**:
  - Improve feedback for network failures, GPS errors, or empty API responses.
- 🧪 **Testing**:
  - Add unit tests for ViewModels and utility functions (e.g., region mapping, distance).
- 🗺️ **Expand Beyond SG**:
  - Allow the app to gracefully notify users when they are outside Singapore and possibly fetch global data in future versions.
- 🚀 **Performance Optimization**:
  - Cache location and weather data to reduce redundant network calls on every refresh.

