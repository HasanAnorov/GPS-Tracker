package com.example.gpstracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gpstracker.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MainActivity : AppCompatActivity(),OnMapReadyCallback {

    lateinit var binding : ActivityMainBinding
    lateinit var gMap :GoogleMap
    lateinit var locationRequest : LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    //lateinit var geocoder: Geocoder

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
        //geocoder = Geocoder(this)

    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.d("myTag","Lat is - " + locationResult.lastLocation.latitude +
                    "Long is - " + locationResult.lastLocation.longitude)
            setUserLocation(locationResult.lastLocation)
        }
    }

    private fun setUserLocation(location:Location){
        val latLng = LatLng(location.latitude,location.longitude)
        if (userLocationMarker==null){
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("User location")
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

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else  {
                ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
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