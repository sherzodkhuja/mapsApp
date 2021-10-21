package com.example.mapapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.mapapp.App
import com.example.mapapp.R
import com.example.mapapp.adapters.PlacesAdapter
import com.example.mapapp.databinding.ActivitySeachPlacesBinding
import com.example.mapapp.models.Place
import com.example.mapapp.utils.Status
import com.example.mapapp.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*
import kotlin.collections.ArrayList

class SeachPlacesActivity : AppCompatActivity(), TextWatcher, OnMapReadyCallback {

    lateinit var binding: ActivitySeachPlacesBinding
    private var mMap: GoogleMap? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var placesAdapter: PlacesAdapter
    private val TAG = "SearchPlacesActivity"

    var polyline: Polyline? = null

    private var whenceText: String = ""
    private var whereText: String = ""

    private var origin: String = ""
    private var destination: String = ""

    private var LocationPermissionsGranted: Boolean = false
    private lateinit var currentLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeachPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LocationPermissionsGranted = intent.getBooleanExtra("permission", false)
        if (LocationPermissionsGranted) {
            currentLocation = intent.getParcelableExtra("current location")!!
            Log.d(TAG, "Current location: $currentLocation")
        }

        val appContainer = (application as App).appContainer
        mainViewModel = appContainer.viewModelFactory.create(MainViewModel::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.search_places_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.backBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.locationWhence.setOnClickListener {
            if (LocationPermissionsGranted) {
                if (whereText != "Your location") {
                    whenceText = "Your location"
                    binding.whenceEt.text?.clear()
                    binding.whenceEt.setText("Your location")

                    binding.whenceEt.keyListener = null
                    binding.rv.visibility = View.GONE
                    if (whereText.isNotEmpty()) {
                        directions(
                            "${currentLocation.latitude},${currentLocation.longitude}",
                            destination
                        )
                    }
                } else {
                    Toast.makeText(this, "Locations cannot be the same!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Allow app to use your current Location!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.locationWhere.setOnClickListener {
            if (LocationPermissionsGranted) {
                if (whenceText != "Your location") {
                    whereText = "Your location"
                    binding.whereEt.text?.clear()
                    binding.whereEt.setText("Your location")
                    binding.whereEt.keyListener = null
                    binding.rv.visibility = View.GONE
                    if (whenceText.isNotEmpty()) {
                        directions(
                            origin,
                            "${currentLocation.latitude},${currentLocation.longitude}"
                        )
                    }
                } else {
                    Toast.makeText(this, "Locations cannot be the same!", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "Allow app to use your current Location!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.whenceEt.setOnClickListener {
            binding.whenceEt.keyListener = android.text.method.TextKeyListener.getInstance()
            if (whenceText != "Your location") {
                binding.whenceEt.setText(whenceText)
                binding.whenceEt.setSelection(binding.whenceEt.length())
                val keyListener = binding.whenceEt.keyListener
            } else {
                origin = ""
                whenceText = ""
                binding.whenceEt.setText("")
            }
        }

        binding.whereEt.setOnClickListener {
            binding.whereEt.keyListener = android.text.method.TextKeyListener.getInstance()
            if (whereText != "Your location") {
                binding.whereEt.setText(whereText)
                binding.whereEt.setSelection(binding.whereEt.length())
            } else {
                destination = ""
                whereText = ""
                binding.whereEt.setText("")
            }
        }
        binding.whenceEt.addTextChangedListener(this)
        binding.whereEt.addTextChangedListener(this)

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        return
    }

    override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
        if (query?.isNotEmpty() == true) {

            binding.rv.visibility = View.VISIBLE
            if (query == binding.whenceEt.editableText) {
                binding.clearWhenceText.visibility = View.VISIBLE
                binding.clearWhenceText.setOnClickListener {
                    origin = ""
                    whenceText = ""
                    binding.whenceEt.text?.clear()
                    binding.clearWhenceText.visibility = View.INVISIBLE
                }
            } else {
                binding.clearWhereText.visibility = View.VISIBLE
                binding.clearWhereText.setOnClickListener {
                    destination = ""
                    whereText = ""
                    binding.whereEt.text?.clear()
                    binding.clearWhereText.visibility = View.INVISIBLE
                }
            }

            mainViewModel.places(query.toString())
                .observe(this, Observer { placesList ->
                    when (placesList.status) {
                        Status.SUCCESS -> {
                            placesAdapter = PlacesAdapter(
                                placesList.data ?: emptyList(),
                                object : PlacesAdapter.OnItemClickListener {
                                    @SuppressLint("MissingPermission")
                                    override fun onItemClick(place: Place, position: Int) {
                                        //set selected country to editText
                                        if (query == binding.whenceEt.editableText) {
                                            if (place.description != whereText) {
                                                whenceText = place.description
                                                origin = place.description
                                                binding.whenceEt.text?.clear()
                                                binding.whenceEt.setText(place.main_text)

                                                binding.whenceEt.keyListener = null
                                                binding.rv.visibility = View.GONE
                                                if (whereText != "") {
                                                    if (whereText == "Your location") {
                                                        directions(
                                                            origin,
                                                            "${currentLocation.latitude},${currentLocation.longitude}"
                                                        )
                                                    } else {
                                                        directions(origin, destination)
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@SeachPlacesActivity,
                                                    "Places cannot be the same!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            if (place.description != whenceText) {
                                                whereText = place.description
                                                destination = place.description
                                                binding.whereEt.text?.clear()
                                                binding.whereEt.setText(place.main_text)
                                                //moving cursor to the end
                                                binding.whereEt.setSelection(binding.whereEt.length())
                                                binding.rv.visibility = View.GONE

                                                if (whenceText != "") {
                                                    if (whenceText == "Your location") {
                                                        directions(
                                                            "${currentLocation.latitude},${currentLocation.longitude}",
                                                            destination
                                                        )
                                                    } else {
                                                        directions(origin, destination)
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@SeachPlacesActivity,
                                                    "Places cannot be the same!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        hideSoftKeyboard()
                                    }
                                })
                            binding.rv.adapter = placesAdapter
                        }
                    }
                })
        } else {
            binding.rv.visibility = View.GONE
            if (query == binding.whenceEt.editableText) {
                binding.clearWhenceText.visibility = View.INVISIBLE
            } else {
                binding.clearWhereText.visibility = View.INVISIBLE
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        return
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
        binding.clearWhenceText.visibility = View.INVISIBLE
        binding.clearWhereText.visibility = View.INVISIBLE
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        mMap?.uiSettings?.isMapToolbarEnabled = false
    }

    private fun directions(origin: String, destination: String) {

        mainViewModel.directions(origin, destination).observe(this, Observer { directionsData ->
            when (directionsData.status) {
                Status.SUCCESS -> {
                    val directionsList: ArrayList<LatLng> = ArrayList()
                    val steps = directionsData.data?.steps
                    for (i in steps?.indices ?: emptyList()) {
                        directionsList.addAll(decodePolyline(steps?.get(i)?.polyline?.points.toString()))
                    }

                    polyline?.remove()
                    mMap?.clear()
                    polyline = mMap?.addPolyline(
                        PolylineOptions()
                            .clickable(true)
                            .color(Color.BLUE)
                            .addAll(directionsList)
                    )
                    if (directionsData.data != null) {
                        val lat = directionsData.data.start_location.lat
                        val lng = directionsData.data.start_location.lng
                        when (directionsData.data.distance.value) {
                            in 0..3000 -> {
                                mMap?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(lat, lng),
                                        14f
                                    )
                                )
                            }
                            in 3000..7000 -> {
                                mMap?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(lat, lng),
                                        13f
                                    )
                                )
                            }
                            in 7000..12000 -> {
                                mMap?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(lat, lng),
                                        12f
                                    )
                                )
                            }
                            in 12000..40000 -> {
                                mMap?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(lat, lng),
                                        11f
                                    )
                                )
                            }
                            else -> {
                                mMap?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(lat, lng),
                                        10f
                                    )
                                )
                            }
                        }
                        mMap?.addMarker(
                            MarkerOptions()
                                .title(directionsData.data.start_address)
                                .position(
                                    LatLng(
                                        directionsData.data.start_location.lat,
                                        directionsData.data.start_location.lng
                                    )
                                )
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pinpng))
                        )?.showInfoWindow()

                        mMap?.addMarker(
                            MarkerOptions()
                                .title(directionsData.data.distance.text)
                                .position(
                                    LatLng(
                                        directionsData.data.end_location.lat,
                                        directionsData.data.end_location.lng
                                    )
                                )
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pinpng))
                        )?.showInfoWindow()
                    }
                }
            }
        })
    }

    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
}