package com.gupta.findmyphone
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_ticket.view.*
import kotlinx.android.synthetic.main.login.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    var listofContact = ArrayList<Contact>()
    var adapter:ContactShowAdapter? = null
    private lateinit var auth: FirebaseAuth
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var myRef:DatabaseReference?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //dummylist()
        adapter =ContactShowAdapter(this,listofContact)
        listcontactMain.adapter = adapter
        myRef = database.reference
        var savedata=SaveData(applicationContext)
        var phoneNumber=savedata.loadphoneNumber()
        auth = FirebaseAuth.getInstance()
        listcontactMain.onItemClickListener= AdapterView.OnItemClickListener { parent, view, position, id ->
            var data=listofContact[position]
            Log.d("Responce-Main", "LINE_41,${data.phn}")
            val df =SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
            val date =Date()
//            Log.d("")
            var phone=SaveData.formatPhoneNumber(data.phn!!)
            myRef!!.child("user").child(phone).child("request").setValue(df.format(Date()))
            var intent=Intent(applicationContext,Map::class.java)
            intent.putExtra("phoneNumber",data.phn)
                startActivity(intent)
        }
          signInAnonymously()

    }

    override fun onResume() {
        super.onResume()
        val user=SaveData(applicationContext)
        if(user.loadphoneNumber().equals("empty"))
        {
            Log.d("Responce-Main-Line59","${user.loadphoneNumber().toString()}")
            return
        }
        val df = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
        val date = Date()
        val phn=SaveData(applicationContext).loadphoneNumber()
        val phone=SaveData.formatPhoneNumber(phn!!.toString())
        myRef!!.child("user").child(phone).child("request").setValue(df.format(date))
        refreashData()
        checkPermissions()
        checkLocationPermissions()
    }

    private fun refreashData() {
        val userData= SaveData(applicationContext)
        Log.d("Responce-Main-Line59","${userData.loadphoneNumber().toString()}")
        val phone=SaveData.formatPhoneNumber(userData.loadphoneNumber().toString())
        myRef!!.child("user").child(phone).child("finder").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    Log.d("responce-Line71,main","${dataSnapshot.childrenCount},${dataSnapshot.children},${dataSnapshot.value}")
                    val td = dataSnapshot!!.value as HashMap<String,Any>
                    listofContact.clear()
                  Log.d("responce-Line74,main","${td.size},${td.keys},${td.values}")
                    if (td.keys == null){
                        listofContact.add(Contact("NO_USERS","nothing"))
                        adapter!!.notifyDataSetChanged()
                        return
                    }
                    for (key in td.keys){
                        var name=AllContactList[key]
                        listofContact.add(Contact(name.toString(),key))
                    }

                    adapter!!.notifyDataSetChanged()
                }catch (ex:Exception){
                    listofContact.clear()
                    listofContact.add(Contact("NO_USERS","nothing"))
                    adapter!!.notifyDataSetChanged()
                    return
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
    val CONTACT_CODE = 123
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), CONTACT_CODE)
            return
        }
        loadcontactlist()
    }
    var LOCATION_CODE=111
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_CODE)
            return
        }
        getLocationOfuser()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CONTACT_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadcontactlist()
                } else {
                    Toast.makeText(this, "Permissions NotGranted", Toast.LENGTH_SHORT).show()
                }
            }
            LOCATION_CODE->{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 getLocationOfuser()
                } else {
                    Toast.makeText(this, "Permissions NotGranted", Toast.LENGTH_SHORT).show()
                }

            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            }
        }
    }


    fun getLocationOfuser() {
        if(!myService.isservicerunning) {
            var intent = Intent(baseContext, myService::class.java)

                    startService(intent)
        }


    }

    val AllContactList=HashMap<String,String>()
    private fun loadcontactlist() {
        try{
            listofContact.clear()
          var cursor=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
          cursor!!.moveToFirst()
          do{
            var name=cursor!!.getString(cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phn=cursor!!.getString(cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            AllContactList.put(phn,name)
          }while (cursor!!.moveToNext())

           }catch (E:Exception){}
    }

    private fun signInAnonymously() {
        auth.signInAnonymously().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(baseContext, "Authentication Success.",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dummylist() {
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
        listofContact.add(Contact("Atul", "123123424"))
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var mymenu=menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.help -> {
                //TODO WORK
            }
            R.id.addTraker -> {
                var intent = Intent(this, myTraker::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    inner class ContactShowAdapter : BaseAdapter {
        var context: Context? = null
        var listofcontact = ArrayList<Contact>()

        constructor(context: Context, listofcontact: ArrayList<Contact>) {
            this.context = context
            this.listofcontact = listofcontact
        }

        override fun getCount(): Int {
            return listofcontact.size
        }

        override fun getItem(position: Int): Any {
            return listofcontact[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val userContact = listofcontact[position]
            if (userContact.name.equals("NO_USERS")) {
                val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val contactTicketView = inflator.inflate(R.layout.no_user_ticket, null)
                return contactTicketView
            }
            else {
                val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val contactTicketView = inflator.inflate(R.layout.contact_ticket, null)
                contactTicketView.contact_name.text = userContact.name.toString()
                contactTicketView.contact_phn_no.text = userContact.phn
                return contactTicketView

            }
        }
    }
}