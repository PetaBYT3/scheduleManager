package com.schedule.rt.sync.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            val serviceIntent = Intent(context, ForegroundService::class.java)
            context?.startService(serviceIntent)
        }
    }
}