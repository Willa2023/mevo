package com.example.mevotest

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mevotest.network.ApiService
import com.example.mevotest.network.FeatureCollection
import com.example.mevotest.network.GeoJson
import com.google.gson.Gson
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(174.7762, -41.2865))
                .pitch(0.0)
                .zoom(11.0)
                .bearing(0.0)
                .build()
        )

        val loadSatellite = findViewById<Button>(R.id.satellite_style)
        loadSatellite.setOnClickListener {
            showSatellite()
        }

        val btnShowVehicle = findViewById<Button>(R.id.button_show_vehicle)
        btnShowVehicle.setOnClickListener {
            showVehicle()
        }
        val btnShowParkZone = findViewById<Button>(R.id.button_show_park_zone)
        btnShowParkZone.setOnClickListener {
            showParkZone()
        }
    }

    private fun showSatellite() {
        mapView.mapboxMap.apply {
            loadStyle(Style.SATELLITE_STREETS) {}
        }
    }

    private fun showVehicle() {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.mevo.co.nz/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        Log.d("willa", apiService.getVehicleData("wellington").request().toString().trimIndent())

        //get Vehicle
        apiService.getVehicleData("wellington").enqueue(object : Callback<FeatureCollection> {
            override fun onResponse(
                call: Call<FeatureCollection>,
                response: Response<FeatureCollection>
            ) {
                Log.d("willa", response.body()?.data.toString())

                mapView.mapboxMap.apply {
                    loadStyle(Style.STANDARD) {
                        addGeoJsonSource(
                            response.body()?.data?.features?.get(0)?.properties?.iconUrl.toString(),
                            Value.fromJson(
                                Gson().toJson(response.body()?.data).trimIndent()
                            ), it
                        )
                    }
                }
            }

            override fun onFailure(call: Call<FeatureCollection>, t: Throwable) {
                Log.d("willa", "failed: " + t.message)
            }
        })
    }

    private fun showParkZone() {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.mevo.co.nz/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        Log.d("willa", apiService.getParkData("wellington").request().toString().trimIndent())

        //get Park Zone
        apiService.getParkData("wellington").enqueue(object : Callback<GeoJson> {
            override fun onResponse(
                call: Call<GeoJson>,
                response: Response<GeoJson>
            ) {
                Log.d("willa", response.body().toString())
                mapView.mapboxMap.loadStyle(
                    style(style = Style.STANDARD) {
                        +geoJsonSource("source-id") {
                            url(Gson().toJson(response.body()?.data))
                        }
                        +layerAtPosition(
                            fillLayer("layer-id", "source-id") {
                                fillColor(Color.parseColor("#0080ff")).fillOpacity(0.0)
                            },
//                            below = SETTLEMENT_LABEL
                        )
                        +lineLayer(
                            "line-layer", "source-id"
                        ) {
                            lineColor(ContextCompat.getColor(this@MainActivity, R.color.black))
                            lineWidth(2.0)
                        }
                    }
                )
            }

            override fun onFailure(call: Call<GeoJson>, t: Throwable) {
                Log.d("willa", "failed: " + t.message)
            }
        })
    }

    private fun addGeoJsonSource(iconUrl: String, geojson: Expected<String, Value>, style: Style) {
        if (geojson.isError) {
            throw RuntimeException("Invalid GeoJson:" + geojson.error)
        }

        val sourceParams = HashMap<String, Value>()
        sourceParams["type"] = Value("geojson")
        sourceParams["data"] = geojson.value!!

        val expected = style.addStyleSource("source", Value(sourceParams))

        if (expected.isError) {
            throw RuntimeException("Invalid GeoJson:" + expected.error)
        }

        Glide.with(this)
            .asBitmap()
            .load(iconUrl)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    style.addImage("custom-icon", resource!!)
                    style.addLayer(
                        symbolLayer("icon_layer", "source") {
                            iconImage("custom-icon")
                            iconAllowOverlap(true)
                            iconAnchor(IconAnchor.BOTTOM)
                        }
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }
}

