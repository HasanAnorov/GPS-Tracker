package com.example.gpstracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gpstracker.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.IOException

class MainActivity : AppCompatActivity(),OnMapReadyCallback {

    lateinit var binding : ActivityMainBinding
    lateinit var gMap :GoogleMap
    lateinit var locationRequest : LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var geocoder: Geocoder
    lateinit var currentLocationForDistance : LatLng
    lateinit var locationForDistance : LatLng

    private var userLocationMarker:Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest= LocationRequest.create()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Searching locations
        init()

    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.d("myTag","Lat is - " + locationResult.lastLocation.latitude +
                    "Long is - " + locationResult.lastLocation.longitude)
            setUserLocation(locationResult.lastLocation)
            currentLocationForDistance = LatLng(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude)
        }
    }

    private fun init(){
        val mSearchText = binding.etSearch
        mSearchText.setOnEditorActionListener { v, actionId, event ->
            if (event != null) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.action == KeyEvent.ACTION_DOWN
                        || event.action == KeyEvent.KEYCODE_ENTER){
                    geoLocate()
                }
            }
            false
        }

        binding.myLocation.setOnClickListener {
            getDeviceLocation()
            hideKeyboard()
        }

        hideKeyboard()
    }

    private fun getDeviceLocation(){

        gMap.clear()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val location = fusedLocationProviderClient.lastLocation
                location.addOnCompleteListener {
                    if (it.isSuccessful){
                        val currentLocation= it.result
                        moveCamera(LatLng(currentLocation.latitude,currentLocation.longitude),15F,"My location",false)
                    }else{
                        Toast.makeText(this, "Cannot get current location", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }catch (e:SecurityException){
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun geoLocate(){
        val searchString = binding.etSearch.text.toString()
        geocoder = Geocoder(this)
        var list = ArrayList<Address>()
        try {
            list = geocoder.getFromLocationName(searchString,1) as ArrayList<Address>
        }catch (e:IOException){
            Log.d( "myTag1","geoLocate : IOException : ${e.localizedMessage}")
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }

        if (list.size>0){
            val address = list[0]
            Log.d("myTag2", "Found locations : ${address.toString()}")

            locationForDistance = LatLng(address.latitude,address.longitude)

            moveCamera(LatLng(address.latitude,address.longitude),15F,address.locality,true)
        }
    }

    private fun moveCamera(latlng:LatLng, zoom:Float,title:String,checkForDistance:Boolean){

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,zoom))

        var reslult = FloatArray(10)

        val option = MarkerOptions()
                .position(latlng)
                .title(title)

        if (checkForDistance){
            Location.distanceBetween(currentLocationForDistance.latitude,currentLocationForDistance.longitude,locationForDistance.latitude,locationForDistance.longitude,reslult)
            option.snippet("Masofa : ${reslult[0]} metr")
        }

        gMap.addMarker(option)

        hideKeyboard()

    }

    private fun setUserLocation(location:Location){
        val latLng = LatLng(location.latitude,location.longitude)
        if (userLocationMarker==null){
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("My location")
            //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.walk))
            userLocationMarker = gMap.addMarker(markerOptions)!!
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17F))
        }else{
            userLocationMarker!!.position = latLng
            //Toast.makeText(this, "address update ", Toast.LENGTH_SHORT).show()
            //gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        gMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.isMyLocationEnabled = true
            gMap.uiSettings.isMyLocationButtonEnabled = false

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else  {
                ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }

        init()

    }

    private fun starLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
    }

    private fun hideKeyboard(){
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            starLocationUpdates()
        }else{
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

}