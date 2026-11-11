package com.sonda11.argus.remote

import android.app.Application

class ArgusRemoteApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
