package com.gupta.findmyphone

import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.login.*
import java.util.*

class newlogin : AppCompatActivity() {

    var database = FirebaseDatabase.getInstance()
    var myRef = database.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newlogin)

        var savedata=SaveData(applicationContext)
        bulogin.setOnClickListener {
            savePhone(etphone.text.toString())
            val df = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
            val date = Date()
            val phone=SaveData.formatPhoneNumber(etphone.text.toString())

            myRef!!.child("user").child(phone.toString()).child("request")
                .setValue(df.format(date))
            myRef!!.child("user").child(phone.toString()).child("finder")
                .child(etphone.text.toString()).setValue(df.format(date))
            var intent= Intent(this,MainActivity::class.java)
            startActivity(intent)

        }

        var phoneNumber=savedata.loadphoneNumber()
        if (!phoneNumber!!.equals("empty"))
        {
            var intent= Intent(this,MainActivity::class.java)
            startActivity(intent)

        }
        else
        {
            Toast.makeText(this, "enter number", Toast.LENGTH_SHORT).show()
        }
    }
    fun savePhone(phone: String?){
        val save=SaveData(applicationContext)
        save.savePhone(phone!!)
    }

}
