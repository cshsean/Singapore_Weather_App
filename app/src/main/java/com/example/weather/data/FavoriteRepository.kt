package com.example.weather.data

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavorites(): Flow<List<FavoriteEntity>>
    fun isFavorite(name: String): Flow<Boolean>
    suspend fun addFavorite(name: String, latitude: Double, longitude: Double)
    suspend fun removeFavorite(name: String)
}