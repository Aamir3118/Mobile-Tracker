package com.example.mobiletracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mobiletracker.databinding.ActivityHomeBinding
import com.example.mobiletracker.databinding.ActivityTrackerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TrackUserActivity : AppCompatActivity() {
    private lateinit var googleMap: GoogleMap
    lateinit var auth: FirebaseAuth
    private lateinit var locationCallback: LocationCallback
    private lateinit var binding: ActivityTrackerBinding
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var databaseReference: DatabaseReference
    private val locationPermissionCode = 1
    private var PERMISSION_ID = 44
    private lateinit var currentUserId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map2) as? SupportMapFragment
//        mapFragment?.getMapAsync(this)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        locationCallback = object : LocationCallback(){
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult.locations.let { locations ->
//                    if (locations.isNotEmpty()) {
//                        val location = locations.first()
//                        val latLng = LatLng(location.latitude, location.longitude)
//                        val markerOptions = MarkerOptions().position(latLng).title("Current Location")
//                        getGoogleMap()?.clear()
//                        getGoogleMap()?.addMarker(markerOptions)
//                        getGoogleMap()?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
//                    }
//                }
//            }
//        }
    }

//    override fun onResume() {
//        super.onResume()
//        if (checkPermissions()) {
//            if (isLocationEnabled()) {
//                startLocationUpdates()
//            } else {
//                Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//            }
//        } else {
//            requestPermissions()
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        stopLocationUpdates()
//    }
//
//    private fun startLocationUpdates() {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            mFusedLocationClient?.requestLocationUpdates(
//                LocationRequest.create()
//                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                    .setInterval(5000)
//                    .setFastestInterval(2000),
//                locationCallback,
//                Looper.getMainLooper()
//            )
//        } else {
//            requestPermissions()
//        }
//    }
//
//    private fun stopLocationUpdates() {
//        mFusedLocationClient?.removeLocationUpdates(locationCallback)
//    }
//
//    private fun checkPermissions(): Boolean {
//        return ActivityCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//
//    }
//
//    private fun requestPermissions() {
//        ActivityCompat.requestPermissions(
//            this, arrayOf(
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ), locationPermissionCode
//        )
//    }
//
//    private fun isLocationEnabled(): Boolean {
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
//            LocationManager.NETWORK_PROVIDER
//        )
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == PERMISSION_ID) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationUpdates()
//            } else {
//                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onMapReady(gMap: GoogleMap) {
//        gMap.let {
//            googleMap = it
//            googleMap.uiSettings.isZoomControlsEnabled = true
//            googleMap.uiSettings.isZoomGesturesEnabled = true
//            googleMap.uiSettings.isCompassEnabled = true
//        }
//    }
//    private fun getGoogleMap(): GoogleMap? {
//        return if (::googleMap.isInitialized) googleMap else null
//    }
}