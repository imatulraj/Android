package com.gupta.findmyphone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.security.Provider

class broadcast:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent!!.action!!.equals("android.intent.action.BOOT_COMPLETED"))
        {
            var intent=Intent(context,myService::class.java)
            context!!.startService(intent)
        }
    }
}