package com.example.mobiletracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobiletracker.databinding.ActivitySearchPersonBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.StyleSpan
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.ref.WeakReference


class SearchPersonActivity : AppCompatActivity(), OnMapReadyCallback {
    var mapFragment: SupportMapFragment? = null
    private lateinit var googleMap: GoogleMap
    lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySearchPersonBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var previousLocation: LatLng
    private val locationPermissionCode = 1
    private lateinit var previousMarkerPosition: LatLng
    private var polyline: Polyline? = null
    private var PERMISSION_ID = 44
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var context: Context? = null
    private var markerAdded = false

    private var savedMarkerPosition: LatLng? = null
    private var savedPolylineOptions: PolylineOptions? = null


    val apiKey = "AIzaSyD2DVDZpsw2tiuhkz1XGREho2x5K4Rl3EI"
    private val userId: String by lazy { intent.getStringExtra("userId") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySearchPersonBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        mapFragment = supportFragmentManager.findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        savedMarkerPosition = googleMap.cameraPosition.target
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        savedMarkerPosition?.let {
            googleMap.addMarker(MarkerOptions().position(it))
        }

        savedPolylineOptions?.let {
            googleMap.addPolyline(it)
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = true

        val userId = intent.getStringExtra("userId") ?: ""
        databaseReference.child("Users").child(userId).addValueEventListener(object :
            ValueEventListener {
            var previousLocation: LatLng? = null

            @SuppressLint("MissingPermission")
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentLocation = LatLng(
                    snapshot.child("latitude").getValue(Double::class.java) ?: 0.0,
                    snapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                )

                if (!markerAdded) {
                    googleMap.addMarker(MarkerOptions().position(currentLocation))
                    markerAdded = true
                }

                databaseReference.child("Users").child(userId).child("Markers").setValue(previousLocation)

                if (previousLocation == null || currentLocation.latitude != previousLocation!!.latitude && currentLocation.longitude != previousLocation!!.longitude) {
                    // Add marker to the current location
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 19f))
                    if (previousLocation != null) {
                        val curve = SphericalUtil.interpolate(previousLocation, currentLocation, 0.25) // creates a curved segment halfway between the two points

                        val polylineOptions = PolylineOptions()
                            .color(Color.BLUE)
                            .add(previousLocation)
                            .add(curve)
                            .add(currentLocation)
                            .width(10f)

                        googleMap.addPolyline(polylineOptions)
//                        googleMap.addPolyline(
//                            PolylineOptions()
//                                .add(previousLocation, currentLocation)
//                                .width(5f)
//                                .color(Color.BLUE)
//                                .geodesic(true)
//                                .addSpan(StyleSpan(Color.GREEN))
//                        )
//                        val urll = getDirectionURL(previousLocation!!, currentLocation, apiKey)
//                        GetDirectionAsyncTask(this).execute(urll)
                    }
                    previousLocation = currentLocation
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("loadPost:onCancelled", error.toException())
            }
        })
        previousLocation = LatLng(0.0, 0.0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedMarkerPosition?.let { outState.putParcelable("markerPosition", it) }
        savedPolylineOptions?.let { outState.putParcelable("polylineOptions", it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedMarkerPosition = savedInstanceState.getParcelable("markerPosition")
        savedPolylineOptions = savedInstanceState.getParcelable("polylineOptions")
    }


    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=walking" +
                "&key=$secret"
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetDirectionAsyncTask(context: ValueEventListener) : AsyncTask<String, Void, PolylineOptions>(){
        private val contextRef: WeakReference<ValueEventListener> = WeakReference(context)
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String?): PolylineOptions? {
            val client = OkHttpClient()
            val request = Request.Builder().url(params[0]!!).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            Log.d("MyResponse",data)

            try {
                val respObj = Gson().fromJson(data, MapData::class.java)
                val path = ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size) {
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                return PolylineOptions().addAll(path).width(10f).color(Color.GREEN).geodesic(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: PolylineOptions?) {
            val context = contextRef.get()
            if (context != null && result != null) {
                googleMap.addPolyline(result)
            } else {
                Log.e("GetDirectionAsyncTask", "Context is null or result is null")
            }
        }
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
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

}

//    private fun getUrl(origin: LatLng, dest: LatLng, mode: String): String {
//        var str_origin = "origin=" + origin.latitude + "," + origin.longitude
//        var str_dest = "destination=" + dest.latitude + "," + dest.longitude
//        val str_mode = "mode=$mode"
//        val parameters = "$str_origin&$str_dest&$str_mode"
//        val output = "json"
//
//        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyD2DVDZpsw2tiuhkz1XGREho2x5K4Rl3EI"
//    }




//    override fun onMapReady(gMap: GoogleMap) {
//        googleMap = gMap
//        mMap = gMap
//        googleMap.uiSettings.isZoomControlsEnabled = true
//        googleMap.uiSettings.isZoomGesturesEnabled = true
//        googleMap.uiSettings.isCompassEnabled = true
//        googleMap.uiSettings.isRotateGesturesEnabled = true
//
//        val userId = intent.getStringExtra("userId") ?: ""
//        databaseReference.child("Users").child(userId).addValueEventListener(object :
//            ValueEventListener {
//            var previousLocation: LatLng? = null
//
//            @SuppressLint("MissingPermission")
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val currentLocation = LatLng(
//                    snapshot.child("latitude").getValue(Double::class.java) ?: 0.0,
//                    snapshot.child("longitude").getValue(Double::class.java) ?: 0.0
//                )
//
//                if (previousLocation == null || currentLocation.latitude != previousLocation!!.latitude && currentLocation.longitude != previousLocation!!.longitude) {
//                    // Add marker to the current location
//                    googleMap.addMarker(MarkerOptions().position(currentLocation))
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 19f))
//                    if (previousLocation != null) {
//                        googleMap.addPolyline(
//                            PolylineOptions()
//                                .add(previousLocation, currentLocation)
//                                .width(5f)
//                                .color(Color.RED)
//                                .geodesic(true)
//                                .addSpan(StyleSpan(Color.GREEN))
//                        )
//                    }
//                    previousLocation = currentLocation
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.w("loadPost:onCancelled", error.toException())
//            }
//        })
//        previousLocation = LatLng(0.0, 0.0)
//    }

