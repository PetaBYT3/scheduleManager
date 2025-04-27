package com.schedule.rt.sync.application

import android.app.Application

class Scheduler : Application() {

    companion object {
        lateinit var instance: Scheduler
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}