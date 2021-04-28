package com.gupta.findmyphone

import android.app.Service
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import com.google.firebase.database.*
import java.util.*

class myService:Service() {
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var myRef: DatabaseReference?=null
    override fun onBind(intent: Intent?): IBinder? {
        return null!!
    }

    override fun onCreate() {
        super.onCreate()
            myRef = database.reference
        isservicerunning=true
    }
    companion object{
        var mylocation: Location?=null
        var isservicerunning=false
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var location= MYlocationListner()
        var locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3,3f,location)
        val phn=SaveData(applicationContext).loadphoneNumber()
    val phone=SaveData.formatPhoneNumber(phn.toString())
        myRef!!.child("user").child(phone).child("request").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                || mylocation!!.latitude==0.0|| mylocation!!.longitude==0.0
                if(myService.mylocation ==null) {
                    return
                }
                myRef!!.child("user").child(phone).child("location").child("lat").setValue(myService.mylocation!!.latitude)
                myRef!!.child("user").child(phone).child("location").child("lon").setValue(myService.mylocation!!.longitude)
                val df = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
                val date = Date()
                myRef!!.child("user").child(phone).child("location").child("Online").setValue(df.format(date))
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

        return Service.START_NOT_STICKY
    }
    inner class MYlocationListner: LocationListener {
        constructor():super(){
             myService.mylocation = Location("me")
            myService.mylocation!!.latitude=0.0
            myService.mylocation!!.longitude=0.0
        }
        override fun onLocationChanged(location: Location) {
            myService.mylocation =location
            val phn=SaveData(applicationContext).loadphoneNumber()
            val phone=SaveData.formatPhoneNumber(phn.toString())
            myRef!!.child("user").child(phone).child("location").child("lat1").setValue(myService.mylocation!!.latitude)
            myRef!!.child("user").child(phone).child("location").child("lon1").setValue(myService.mylocation!!.longitude)
        }


        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }

        override fun onProviderDisabled(provider: String) {

        }

        override fun onProviderEnabled(provider: String) {

        }



    }

}