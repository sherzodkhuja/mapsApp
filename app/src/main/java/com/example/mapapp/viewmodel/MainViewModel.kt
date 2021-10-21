package com.example.mapapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.mapapp.models.Directions
import com.example.mapapp.models.LatLongitude
import com.example.mapapp.models.Place
import com.example.mapapp.repository.MapRepository
import com.example.mapapp.utils.Resource

class MainViewModel(private val mapRepository: MapRepository): ViewModel() {

    fun places(query: String): LiveData<Resource<List<Place>>>{
        return mapRepository.getPlaces(query)
    }

    fun latitudeLongitude(address: String): LiveData<Resource<LatLongitude>>{
        return mapRepository.getLatitudeLongitude(address)
    }

    fun directions(origin: String, destination: String): LiveData<Resource<Directions>>{
        return mapRepository.getDirections(origin, destination)
    }
}