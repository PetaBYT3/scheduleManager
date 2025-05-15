package com.schedule.rt.sync.application

import android.app.Application
import com.schedule.rt.sync.userpreferences.SettingsPreferences

class Scheduler : Application() {
    lateinit var settingsPreferences: SettingsPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        settingsPreferences = SettingsPreferences(applicationContext)
    }
}