package com.example.weather

import android.app.Application
import android.util.Log
import com.example.weather.data.AppContainer
import com.example.weather.data.DefaultAppContainer
import com.example.weather.data.LocationDatabase
import com.example.weather.data.LocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class WeatherApplication: Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // Insert the JSON data into the database only once
        preloadLocationsIfNeeded()
    }

    private fun preloadLocationsIfNeeded() {
        val db = LocationDatabase.getDatabase(this)
        val dao = db.locationDao()

        CoroutineScope(Dispatchers.IO).launch {
            val existing = dao.getAllLocationsOnce() // one-time fetch
            if (existing.isEmpty()) {
                val json = assets.open("sg_locations.json")
                    .bufferedReader().use { it.readText() }

                val locations = JSONArray(json).let { array ->
                    List(array.length()) { i ->
                        val obj = array.getJSONObject(i)
                        LocationEntity(
                            name = obj.getString("name"),
                            latitude = obj.getDouble("latitude"),
                            longitude = obj.getDouble("longitude")
                        )
                    }
                }

                dao.insertAll(locations)
                Log.d("Preload", "Inserted ${locations.size} locations.")
            }
        }
    }
}