package com.gupta.findmyphone

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_my_traker.*
import kotlinx.android.synthetic.main.contact_ticket.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class myTraker : AppCompatActivity() {
    var listofContact = ArrayList<Contact>()
    var adapter: ContactShowAdapter? = null
    var saveData:SaveData?=null
    var database = FirebaseDatabase.getInstance()
    var myRef:DatabaseReference?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_traker)
        //dummylist()
        myRef= database.reference
        saveData= SaveData(applicationContext)
        adapter = ContactShowAdapter(this, listofContact)
        listcontact.adapter = adapter
        listcontact.onItemClickListener=AdapterView.OnItemClickListener { parent, view, position, id ->
            var data=listofContact[position]
            Log.d("Responce", "LINE_37,${data.phn}")
            SaveData.myTrackers.remove(data.phn)
            saveData!!.savephninSherf()
            refereshdata()
            val userdata=SaveData(applicationContext)
            Log.d("responce -Line162","${data.phn},${userdata.loadphoneNumber()}")
            myRef!!.child("user").child(SaveData.formatPhoneNumber(data.phn!!)).child("finder").child(SaveData.formatPhoneNumber(userdata.loadphoneNumber()!!.toString())).removeValue()

        }
        Log.d("Responce", "LINE_41")
        saveData!!.loaddata()
        refereshdata()
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
        var mymenu = menuInflater.inflate(R.menu.mytraker, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finish -> {
                this.finish()
            }
            R.id.add -> {
                checkPermissions()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    val CONTACT_CODE = 123
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), CONTACT_CODE)
            return
        }
        loadcontact()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CONTACT_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadcontact()
                } else {
                    Toast.makeText(this, "Permissions NotGranted", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            }
        }
    }

    val PickCode = 1234
    private fun loadcontact() {
        var intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, PickCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PickCode -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {

                    val contactData = data!!.data
                    val c = contentResolver.query(contactData!!, null, null, null, null)

                    if (c!!.moveToFirst()) {

                        val id =
                            c!!.getString(c!!.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val hasPhone =
                            c!!.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        if (hasPhone.equals("1")) {
                            val phones = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                                null,
                                null
                            )

                            phones!!.moveToFirst()
                            var phoneNumber = phones!!.getString(phones!!.getColumnIndex("data1"))
                            val name =
                                c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                            listofContact.add(Contact(name, phoneNumber))
//                            Log.d("Responce", "Line126 list is updated")
//                            adapter!!.notifyDataSetChanged()

                            SaveData.myTrackers.put(phoneNumber, name)
                            saveData!!.savephninSherf()
                            refereshdata()
                            val userdata=SaveData(applicationContext)
                            Log.d("responce -Line162","${phoneNumber!!} , ${phoneNumber.javaClass},${userdata.loadphoneNumber()}")
                            val df=SimpleDateFormat("dd/mm/yyyy hh:mm:ss a")
                            val date=Date()
                            myRef!!.child("user").child(SaveData.formatPhoneNumber(phoneNumber!!)).child("finder").child(SaveData.formatPhoneNumber(userdata.loadphoneNumber()!!.toString())).setValue(System.currentTimeMillis())
                        }

                    }

                }

            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    fun refereshdata(){
        listofContact.clear()
        for ((key, value) in SaveData.myTrackers)
        {
            Log.d("Responce", "LINE_155-${value}-${key}")
            listofContact.add(Contact(value, key))
        }
        Log.d("Responce", "Line-158")
        adapter!!.notifyDataSetChanged()
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
            val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val contactTicketView = inflator.inflate(R.layout.contact_ticket, null)
            contactTicketView.contact_name.text = userContact.name.toString()
            contactTicketView.contact_phn_no.text = userContact.phn
            return contactTicketView

        }
    }
}
