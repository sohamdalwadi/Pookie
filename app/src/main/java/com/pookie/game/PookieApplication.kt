package com.pookie.game

import android.app.Application
import com.pookie.game.di.ServiceLocator

class PookieApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(this)
    }
}
