package com.example.mapapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mapapp.models.Directions
import com.example.mapapp.models.LatLongitude
import com.example.mapapp.models.Place
import com.example.mapapp.networkcheck.NetworkHelper
import com.example.mapapp.retrofit.ApiService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import com.example.mapapp.utils.Resource
import java.lang.Exception

class MapRepository(
    private val apiService: ApiService,
    private val networkHelper: NetworkHelper
) {
    private val places = MutableLiveData<Resource<List<Place>>>()
    private val latLng = MutableLiveData<Resource<LatLongitude>>()
    private val directionsList = MutableLiveData<Resource<Directions>>()

    private var query: String? = null
    private var address: String? = null

    private var origin: String? = null
    private var destination: String? = null

    private fun loadPlaces() {
        if (networkHelper.isNetworkConnected()) {
            GlobalScope.launch {
                places.postValue(Resource.loading(null))

                try {
                    val a = async { apiService.getPlaces(query ?: "tashkent") }
                    val list = a.await()
                    val placesList = ArrayList<Place>()

                    list.predictions.forEach { placeResponse ->
                        val place = Place(
                            placeResponse.description,
                            placeResponse.structured_formatting.main_text,
                            placeResponse.structured_formatting.secondary_text
                        )
                        placesList.add(place)
                        places.postValue(Resource.success(placesList))
                    }

                } catch (e: Exception) {
                    places.postValue(Resource.error(e.message ?: "Error", null))
                }
            }
        } else {
            places.postValue(Resource.error("No Internet Connection!", null))
        }
    }

    private fun loadLatLong() {
        if (networkHelper.isNetworkConnected()) {
            GlobalScope.launch {
                latLng.postValue(Resource.loading(null))
                try {
                    val a = async { apiService.getLatLong(address ?: "tashkent") }
                    val latitudeLongitude = a.await()
                    latitudeLongitude.results.forEach {
                        val location =  it.geometry.location
                        latLng.postValue(Resource.success(LatLongitude(location.lat, location.lng)))
                    }
                } catch (e: Exception) {
                    latLng.postValue(Resource.error(e.message ?: "Error", null))
                }
            }
        } else {
            latLng.postValue(Resource.error("No Internet Connection!", null))
        }
    }

    private fun loadDirections(){
        if (networkHelper.isNetworkConnected()) {
            GlobalScope.launch {
                directionsList.postValue(Resource.loading(null))
                try {
                    val a = async { apiService.getDirections(origin?:"", destination?:"") }
                    val directionsResponse = a.await()
                    val directionsData = directionsResponse.routes[0].legs[0]
                    val directions = Directions(directionsData.distance, directionsData.duration, directionsData.end_address, directionsData.end_location, directionsData.start_address, directionsData.start_location, directionsData.steps)
                    directionsList.postValue(Resource.success(directions))
                } catch (e: Exception) {
                    latLng.postValue(Resource.error(e.message ?: "Error", null))
                }
            }
        } else {
            latLng.postValue(Resource.error("No Internet Connection!", null))
        }
    }

    fun getPlaces(query: String): LiveData<Resource<List<Place>>> {
        this.query = query
        loadPlaces()
        return places
    }

    fun getLatitudeLongitude(address: String): LiveData<Resource<LatLongitude>> {
        this.address = address
        loadLatLong()
        return latLng
    }

    fun getDirections(origin: String, destination: String): LiveData<Resource<Directions>> {
        this.origin = origin
        this.destination = destination
        loadDirections()
        return directionsList
    }
}