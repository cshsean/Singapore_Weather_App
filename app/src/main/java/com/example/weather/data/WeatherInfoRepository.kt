package com.example.weather.data

import com.example.weather.model.ForecastResponse
import com.example.weather.model.HourlyForecastResponse
import com.example.weather.model.RainfallResponse
import com.example.weather.model.TemperatureResponse
import com.example.weather.model.UVIndexResponse
import com.example.weather.model.WindSpeedResponse
import com.example.weather.network.NEAApiService

interface WeatherInfoRepository {
    suspend fun getTwoHourForecast(): ForecastResponse
    suspend fun getUVIndex(): UVIndexResponse
    suspend fun get24HourForecast(): HourlyForecastResponse
    suspend fun getTemperature(): TemperatureResponse
    suspend fun getWindSpeed(): WindSpeedResponse
    suspend fun getRainfall(): RainfallResponse
}

class NetworkWeatherInfoRepository(
    private val neaApiService: NEAApiService
): WeatherInfoRepository {
    override suspend fun getTwoHourForecast(): ForecastResponse = neaApiService.getTwoHourForecast()
    override suspend fun getUVIndex(): UVIndexResponse = neaApiService.getUVIndex()
    override suspend fun get24HourForecast(): HourlyForecastResponse = neaApiService.get24HourForecast()
    override suspend fun getTemperature(): TemperatureResponse = neaApiService.getTemperature()
    override suspend fun getWindSpeed(): WindSpeedResponse = neaApiService.getWindSpeed()
    override suspend fun getRainfall(): RainfallResponse = neaApiService.getRainFall()
}