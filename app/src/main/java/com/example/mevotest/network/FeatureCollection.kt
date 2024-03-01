package com.example.mevotest.network

data class FeatureCollection(
    val type: String,
    val data: Data
)

data class Data(
    val type: String,
    val features: List<Feature>
)

data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

data class Properties(
    val iconUrl: String
)

