package com.example.weather.network

// import com.example.weather.model.WindSpeedResponse
import com.example.weather.model.ForecastResponse
import com.example.weather.model.HourlyForecastResponse
import com.example.weather.model.RainfallResponse
import com.example.weather.model.TemperatureResponse
import com.example.weather.model.UVIndexResponse
import com.example.weather.model.WindSpeedResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NEAApiService {
    @GET("environment/2-hour-weather-forecast")
    suspend fun getTwoHourForecast(): ForecastResponse

    @GET("environment/uv-index")
    suspend fun getUVIndex(): UVIndexResponse

    @GET("environment/24-hour-weather-forecast")
    suspend fun get24HourForecast(): HourlyForecastResponse

    @GET("environment/air-temperature")
    suspend fun getTemperature(): TemperatureResponse

    @GET("environment/wind-speed")
    suspend fun getWindSpeed(): WindSpeedResponse

    @GET("environment/rainfall")
    suspend fun getRainFall(): RainfallResponse
}