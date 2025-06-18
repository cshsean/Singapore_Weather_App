package com.example.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 2 HOUR WEATHER FORECAST **/
@Serializable
data class ForecastResponse(
    @SerialName("area_metadata")
    val areaMetaData: List<ForecastResponseMetadata>,
    val items: List<ForecastResponseItem>,
    @SerialName("api_info")
    val apiInfo: ApiInfo
)

@Serializable
data class ForecastResponseMetadata(
    val name: String,
    @SerialName("label_location")
    val labelLocation: Location
)

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class ForecastResponseItem(
    @SerialName("update_timestamp")
    val updateTimestamp: String,
    val timestamp: String,
    @SerialName("valid_period")
    val validPeriod: ValidPeriod,
    val forecasts: List<ForecastResponseWeather>
)

@Serializable
data class ValidPeriod(
    val start: String,
    val end: String
)

@Serializable
data class ForecastResponseWeather (
    val area: String,
    val forecast: String
)

@Serializable
data class ApiInfo(
    val status: String
)


/** WIND SPEED **/
@Serializable
data class WindSpeedResponse(
    val metadata: WindSpeedMetadata,
    val items: List<WindSpeedList>,
    @SerialName("api_info")
    val apiInfo: ApiInfo
)

@Serializable
data class WindSpeedMetadata(
    val stations: List<Station>,
    @SerialName("reading_type")
    val readingType: String,
    @SerialName("reading_unit")
    val readingUnit: String
)

@Serializable
data class Station(
    val id: String,
    @SerialName("device_id")
    val deviceId: String,
    val name: String,
    val location: Location
)

@Serializable
data class WindSpeedList(
    val timestamp: String,
    val readings: List<StationReading>
)

@Serializable
data class StationReading(
    @SerialName("station_id")
    val stationId: String,
    val value: Float
)

/** UV INDEX **/
@Serializable
data class UVIndexResponse(
    val items: List<UVIndexItem>,
    @SerialName("api_info")
    val apiInfo: ApiInfo
)

@Serializable
data class UVIndexItem(
    val timestamp: String,
    @SerialName("update_timestamp")
    val updateTimestamp: String,
    val index: List<UVIndexHistory>
)

@Serializable
data class UVIndexHistory(
    val value: Int,
    val timestamp: String
)

/** 24 HOUR WEATHER FORECAST **/
@Serializable
data class HourlyForecastResponse(
    val items: List<HourlyForecastResponseItems>,
    @SerialName("api_info")
    val apiInfo: ApiInfo
)

@Serializable
data class HourlyForecastResponseItems(
    @SerialName("update_timestamp")
    val updateTimestamp: String,
    val timestamp: String,
    @SerialName("valid_period")
    val validPeriod: ValidPeriod,
    val general: HourlyForecastResponseGeneral,
    val periods: List<HourlyForecastResponsePeriod>,
)

@Serializable
data class HourlyForecastResponseGeneral(
    val forecast: String,
    @SerialName("relative_humidity")
    val relativeHumidity: HighLow,
    val temperature: HighLow,
    val wind: HourlyWind
)

@Serializable
data class HighLow(
    val low: Int,
    val high: Int
)

@Serializable
data class HourlyWind(
    val speed: HighLow,
    val direction: String
)

@Serializable
data class HourlyForecastResponsePeriod(
    val time: HourlyTime,
    val regions: RegionWeather
)

@Serializable
data class HourlyTime(
    val start: String,
    val end: String
)

@Serializable
data class RegionWeather(
    val west: String,
    val east: String,
    val central: String,
    val south: String,
    val north: String
)

/** TEMPERATURE **/
@Serializable
data class TemperatureResponse(
    val metadata: TemperatureMetadata,
    val items: List<TemperatureItem>,
    @SerialName("api_info") val apiInfo: ApiInfo
)

@Serializable
data class TemperatureMetadata(
    val stations: List<TemperatureStation>,
    @SerialName("reading_type") val readingType: String,
    @SerialName("reading_unit") val readingUnit: String
)

@Serializable
data class TemperatureStation(
    val id: String,
    @SerialName("device_id") val deviceId: String,
    val name: String,
    val location: TemperatureLocation
)

@Serializable
data class TemperatureLocation(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class TemperatureItem(
    val timestamp: String,
    val readings: List<TemperatureReading>
)

@Serializable
data class TemperatureReading(
    @SerialName("station_id") val stationId: String,
    val value: Double
)

/** RAINFALL **/
@Serializable
data class RainfallResponse(
    val metadata: Metadata,
    val items: List<Item>,
    @SerialName("api_info")
    val apiInfo: ApiInfo
)

@Serializable
data class Metadata(
    val stations: List<Station>
)

@Serializable
data class Item(
    val timestamp: String,
    val readings: List<Reading>
)

@Serializable
data class Reading(
    @SerialName("station_id")
    val stationId: String,
    val value: Double
)
