package com.example.mapapp.models.latlng

data class Geometry(
    val location: Location,
    val location_type: String,
    val viewport: Viewport
)