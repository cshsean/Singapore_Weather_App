package com.example.weather.ui.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weather.data.datastore.SettingsDataStore
import kotlinx.coroutines.launch

class SettingsScreenViewModel(private val context: Context) : ViewModel() {
    var isRainSoundEnabled by mutableStateOf(true)
        private set
    var isImperialUnitEnabled by mutableStateOf(false)
        private set
    var isDarkMode by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            SettingsDataStore.getRainSoundFlow(context).collect { saved ->
                isRainSoundEnabled = saved
            }
            SettingsDataStore.getUnitsFlow(context).collect { saved ->
                isImperialUnitEnabled = saved
            }
            SettingsDataStore.getDarkModeFlow(context).collect { saved ->
                isDarkMode = saved
            }
        }
    }

    fun toggleRainSound() {
        val newValue = !isRainSoundEnabled
        isRainSoundEnabled = newValue
        viewModelScope.launch {
            SettingsDataStore.setRainSoundEnabled(context, newValue)
        }
    }

    fun toggleImperialUnit() {
        val newValue = !isImperialUnitEnabled
        isImperialUnitEnabled = newValue
        viewModelScope.launch {
            SettingsDataStore.setUnits(context, newValue)
        }
    }

    fun toggleDarkMode() {
        val newValue = !isDarkMode
        isDarkMode = newValue
        viewModelScope.launch {
            SettingsDataStore.setDarkMode(context, newValue)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsScreenViewModel(context.applicationContext) as T
        }
    }
}