package com.gupta.findmyphone

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SaveData {
    var context: Context? = null
    var sharedref: SharedPreferences? = null

    constructor(context: Context) {
        this.context = context
        sharedref = context!!.getSharedPreferences("userPhone", Context.MODE_PRIVATE)
    }


    fun savePhone(phoneNumber: String) {
        val editor = sharedref!!.edit()
        val phoneumber=SaveData.formatPhoneNumber(phoneNumber).toString()
        editor.putString("phoneNumber", phoneumber)
        editor.commit()
    }

    fun loadphoneNumber(): String? {
        var phoneNumber = sharedref!!.getString("phoneNumber", "empty")
        return phoneNumber
    }
    fun savephninSherf()
    {
        var list=""
        for((key,value) in myTrackers)
        {
            if(list.length==0)
            {
                list=key+"%"+value
            }
            else
            {
                list+="%"+key+"%"+value
            }
        }
        if(list.length==0)
        {
            list="empty"
        }
        val editor=sharedref!!.edit()
        editor.putString("ContactDetails",list)
        editor.commit()
    }
    fun loaddata()
    {
        myTrackers.clear()
        var list=sharedref!!.getString("ContactDetails","empty").toString()
        Log.d("Responce",list.toString())
        if(!list.equals("empty"))
        {
            var value=list!!.split("%").toTypedArray()
            var i=0
            while (i<value.size)
            {
                Log.d("Responce","$i-${value[i]}-${value[i+1]}")
                myTrackers.put(value[i],value[i+1])
                i+=2
            }
        }
    }
    companion object{
        var myTrackers: MutableMap<String,String> = HashMap()
        fun formatPhoneNumber(phoneNumber:String):String {
            var onlyNumber= phoneNumber.replace("[^0-9]".toRegex(),"")
            if (phoneNumber[0]=='+') {
                onlyNumber =phoneNumber.substring(3)
            }

            return  onlyNumber
        }
    }
}
