package com.example.mevotest.network
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("public/vehicles/{cityName}")
    fun getVehicleData(@Path("cityName") cityName: String): Call<FeatureCollection>

    @GET("public/parking/{cityName}")
    fun getParkData(@Path("cityName") cityName: String): Call<GeoJson>
}

