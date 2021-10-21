package com.example.mapapp.retrofit

import com.example.mapapp.models.Directions
import com.example.mapapp.models.directions.DirectionsResponse
import com.example.mapapp.models.place.PlaceResponse
import com.example.mapapp.models.latlng.PlaceLatLong
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("place/autocomplete/json")
    suspend fun getPlaces(
        @Query("input") input: String = "tashkent",
        @Query("key") api_key: String = "AIzaSyA2l_i8cZq2dIvR5X2ISF7z04mbw0PaJGk"
    ): PlaceResponse

    @GET("geocode/json")
    suspend fun getLatLong(
        @Query("address") address: String = "tashkent",
        @Query("key") api_key: String = "AIzaSyA2l_i8cZq2dIvR5X2ISF7z04mbw0PaJGk"
    ): PlaceLatLong

    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") api_key: String = "AIzaSyA2l_i8cZq2dIvR5X2ISF7z04mbw0PaJGk"
    ): DirectionsResponse

}