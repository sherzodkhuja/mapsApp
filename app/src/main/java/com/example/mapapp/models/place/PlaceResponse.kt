package com.example.mapapp.models.place

data class PlaceResponse(
    val predictions: List<Prediction>,
    val status: String
)