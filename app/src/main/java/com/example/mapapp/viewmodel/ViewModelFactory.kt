package com.example.mapapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mapapp.repository.MapRepository
import java.lang.IllegalArgumentException

class ViewModelFactory(private val mapRepository: MapRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mapRepository) as T
        }

        throw IllegalArgumentException("Error")
    }
}