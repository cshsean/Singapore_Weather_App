package com.example.weather.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class OfflineFavoriteRepository(
    private val favoriteDao: FavoriteDao
): FavoriteRepository {
    override fun getFavorites(): Flow<List<FavoriteEntity>> =
        favoriteDao.getFavorites()

    override fun isFavorite(name: String): Flow<Boolean> =
        favoriteDao.isFavorite(name)

    override suspend fun addFavorite(name: String, latitude: Double, longitude: Double) =
        favoriteDao.addFavorite(FavoriteEntity(name = name, latitude = latitude, longitude = longitude))

    override suspend fun removeFavorite(name: String) =
        favoriteDao.removeFavorite(name)
}