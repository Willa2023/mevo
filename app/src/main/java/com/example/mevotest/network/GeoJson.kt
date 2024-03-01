package com.example.mevotest.network

data class GeoJson(
    val type: String,
    val data: GeoJsonData
)

data class GeoJsonData(
    val type: String,
    val geometry: NewGeometry
)

data class NewGeometry(
    val type: String,
    val coordinates: List<List<List<Double>>>
)