package com.example.mapapp

import android.content.Context
import com.example.mapapp.networkcheck.NetworkHelper
import com.example.mapapp.repository.MapRepository
import com.example.mapapp.retrofit.ApiClient
import com.example.mapapp.retrofit.ApiService
import com.example.mapapp.viewmodel.ViewModelFactory

class AppContainer(private val context: Context) {

    private val apiService: ApiService = ApiClient.apiService
    private val networkHelper = NetworkHelper(context)

    private val mapRepository = MapRepository(apiService, networkHelper)
    val viewModelFactory = ViewModelFactory(mapRepository)
}