package com.example.mapapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.mapapp.App
import com.example.mapapp.R
import com.example.mapapp.adapters.PlacesAdapter
import com.example.mapapp.databinding.ActivityMapBinding
import com.example.mapapp.models.Place
import com.example.mapapp.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.example.mapapp.utils.Status
import java.util.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityMapBinding
    private var mMap: GoogleMap? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var placesAdapter: PlacesAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var DEFAULT_ZOOM = 15f

    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private var LocationPermissionsGranted = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234

    private var locationText: String = ""

    private var currentLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appContainer = (application as App).appContainer
        mainViewModel = appContainer.viewModelFactory.create(MainViewModel::class.java)

        getLocationPermission()

        binding.searchTxt.setOnClickListener {
            binding.searchTxt.keyListener = android.text.method.TextKeyListener.getInstance()
            binding.searchTxt.setText(locationText)
            binding.searchTxt.setSelection(binding.searchTxt.length())
        }

        binding.navigateBtn.setOnClickListener {
            val intent = Intent(this, SeachPlacesActivity::class.java)
            intent.putExtra("permission", LocationPermissionsGranted)
            intent.putExtra("current location", currentLocation)
            startActivity(intent)
        }

        binding.myGeoLocationBtn.setOnClickListener {
            getCurrentLocation()
        }

        binding.searchTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                query: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                return
            }

            override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
                if (query?.isNotEmpty() == true) {
                    binding.rv.visibility = View.VISIBLE
                    binding.clearText.visibility = View.VISIBLE

                    binding.clearText.setOnClickListener {
                        binding.searchTxt.text?.clear()
                        binding.clearText.visibility = View.INVISIBLE
                        binding.progressbar.visibility = View.INVISIBLE
                        locationText = ""
                    }

                    mainViewModel.places(query.toString())
                        .observe(this@MapActivity, Observer { placesList ->
                            when (placesList.status) {
                                Status.SUCCESS -> {
                                    binding.progressbar.visibility = View.INVISIBLE
                                    placesAdapter = PlacesAdapter(
                                        placesList.data ?: emptyList(),
                                        object : PlacesAdapter.OnItemClickListener {
                                            override fun onItemClick(place: Place, position: Int) {
                                                locationText = place.main_text
                                                //set selected country to editText
                                                binding.searchTxt.text?.clear()
                                                binding.searchTxt.setText(place.main_text)
                                                binding.searchTxt.keyListener = null

                                                binding.rv.visibility = View.GONE

                                                mainViewModel.latitudeLongitude(place.description)
                                                    .observe(this@MapActivity, Observer {
                                                        when (it.status) {
                                                            Status.SUCCESS -> {
                                                                if (it != null) {
                                                                    mMap?.clear()
                                                                    moveCamera(
                                                                        LatLng(
                                                                            it.data?.lat!!,
                                                                            it.data.lng
                                                                        ), 15f
                                                                    )
                                                                }
                                                            }
                                                            Status.ERROR -> {
                                                                Toast.makeText(
                                                                    this@MapActivity,
                                                                    "Unknown Place",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    })
                                            }
                                        })
                                    binding.rv.adapter = placesAdapter
                                }
                                Status.LOADING -> {
                                    binding.progressbar.visibility = View.VISIBLE
                                }
                            }
                        })
                } else {
                    binding.rv.visibility = View.GONE
                    binding.clearText.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(query: Editable?) {
                return
            }
        })

    }

    private fun initMap() {
        Log.d(ContentValues.TAG, "initMap: initializing map")
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.my_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun getCurrentLocation() {
        Log.d(ContentValues.TAG, "getDeviceLocation: getting the devices current location")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (LocationPermissionsGranted) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            currentLocation = LatLng(location.latitude, location.longitude)
                            //getting the name of current location
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val addresses =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null) {
                                //showing current location's name with animation
                                val addressName = addresses[0]
                                binding.locationTxtLinear.visibility = View.VISIBLE
                                val anim: Animation = AnimationUtils.loadAnimation(
                                    this,
                                    R.anim.geo_location_text_anim
                                )
                                binding.myLocationTxt.startAnimation(anim)
                                binding.myLocationTxt.text =
                                    "${addressName.locality}, ${addressName.thoroughfare}, ${addressName.featureName} (${addressName.countryName})"
                                Log.d(ContentValues.TAG, "current location is: $addressName")

                                Handler().postDelayed(Runnable {
                                    binding.locationTxtLinear.visibility = View.GONE
                                }, 5000)
                            }
                            mMap?.clear()
                            moveCamera(
                                LatLng(location.latitude, location.longitude),
                                DEFAULT_ZOOM
                            )
                        } else {
                            Toast.makeText(this, "Turn on device's Location!!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: SecurityException) {
                Log.e(ContentValues.TAG, "getDeviceLocation: SecurityException: " + e.message)
            }
        } else {
            getLocationPermission()
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        Log.d(
            ContentValues.TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        hideSoftKeyboard()
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        binding.clearText.visibility = View.VISIBLE

        mMap?.addMarker(
            MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pinpng))
        )
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
        binding.clearText.visibility = View.INVISIBLE
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(ContentValues.TAG, "onMapReady: map is ready")
        mMap = googleMap

        if (LocationPermissionsGranted) {
            getCurrentLocation()
            //little blue dot appears on my location
            mMap?.isMyLocationEnabled = true
            mMap?.uiSettings?.isMyLocationButtonEnabled = false
        } else {
            Toast.makeText(this, "Allow app to access device's Location!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getLocationPermission() {
        Log.d(ContentValues.TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    COURSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                LocationPermissionsGranted = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                initMap()
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
            initMap()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(ContentValues.TAG, "onRequestPermissionsResult: called.")
        LocationPermissionsGranted = false

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    for (i in grantResults.indices) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            LocationPermissionsGranted = false
                            Log.d(
                                ContentValues.TAG,
                                "onRequestPermissionsResult: permission failed"
                            )
                            return
                        }
                    }
                    Log.d(ContentValues.TAG, "onRequestPermissionsResult: permission granted");
                    LocationPermissionsGranted = true
                    //initialize our map
                    initMap()
                } else {
                    initMap()
                }
            }
        }
    }
}