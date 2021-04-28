package com.gupta.findmyphone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import java.lang.Exception

class Map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
        var myRef:DatabaseReference?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        var b:Bundle= intent.extras!!
        myRef=FirebaseDatabase.getInstance().reference
        var phn=b.getString("phoneNumber")
        Log.d("respone-Map-28","")

        myRef!!.child("user").child(SaveData.formatPhoneNumber(phn.toString())).child("location").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
//                try {
                    Log.d("respone-Map-38","DATA VALUE CHANGE")
                    Log.d("respone-Map-38","${snapshot.childrenCount},${snapshot.children},${snapshot.value},")

                    var td = snapshot.value as HashMap<String, Any>

                        var lat = td["lat"] as Double
                        var long = td["lon"] as Double
                        Map.lastOnline = td["Online"] as String
                        Log.d("respone-Map-40", "$lat,$long,$lastOnline")
                        Toast.makeText(this@Map, "$lat $long", Toast.LENGTH_SHORT).show()
                        Map.sydney = LatLng(lat!!.toDouble(), long!!.toDouble())


                loadmap()
            }
        })
    }
    fun loadmap(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    companion object{
        var sydney = LatLng(34.0, 80.0)
        var lastOnline=""
        var map:GoogleMap?=null
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.addMarker(MarkerOptions()
            .position(sydney)
            .title(lastOnline)
            .snippet(lastOnline))
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))

        // Add a marker in Sydney and move the camera


    }
}