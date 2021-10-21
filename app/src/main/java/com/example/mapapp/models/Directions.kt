package com.example.mapapp.models

import com.example.mapapp.models.directions.*

data class Directions(
    val distance: Distance,
    val duration: Duration,
    val end_address: String,
    val end_location: EndLocation,
    val start_address: String,
    val start_location: StartLocation,
    val steps: List<Step>,
)