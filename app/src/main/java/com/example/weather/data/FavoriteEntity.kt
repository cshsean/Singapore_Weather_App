package com.example.weather.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "favorites",
    indices = [Index(value = ["name"], unique = true)]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
