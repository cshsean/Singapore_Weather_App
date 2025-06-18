package com.example.weather.location

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
): LocationClient {

    private val _locationStatusFlow = MutableSharedFlow<Boolean>(replay = 1)
    override val locationStatusFlow: Flow<Boolean> = _locationStatusFlow

    @SuppressLint("MissingPermission")
    override fun getLocationUpdate(interval: Long): Flow<Location> {
        return callbackFlow {
            if (!context.hasLocationPermission()) {
                throw LocationClient.LocationException("Missing location permission")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGpsEnabled && !isNetworkEnabled) {
                throw LocationClient.LocationException("GPS is disabled")
            }

            val request = LocationRequest.Builder(interval)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)   // ← make sure you explicitly request GPS
                .setMinUpdateIntervalMillis(interval)
                .build()

            val locationCallBack = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallBack,
                Looper.getMainLooper()
            )

            context.registerReceiver(
                locationStateReceiver,
                IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            )

            awaitClose {
                client.removeLocationUpdates(locationCallBack)
                context.unregisterReceiver(locationStateReceiver)
            }
        }
    }

    private val locationStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (isGpsEnabled || isNetworkEnabled) {
                    // Notify app to retry or resume location updates
                    _locationStatusFlow.tryEmit(true)
                } else {
                    _locationStatusFlow.tryEmit(false)
                }
            }
        }
    }
}