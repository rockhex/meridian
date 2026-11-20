package com.meridian.app.di

import android.app.Application
import android.content.Context

class MeridianApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notifications.createChannels()
        container.syncScheduler.schedulePeriodic()
    }
}

val Context.container: AppContainer
    get() = (applicationContext as MeridianApplication).container
