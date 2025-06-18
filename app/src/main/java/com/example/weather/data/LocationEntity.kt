package com.example.weather.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)


