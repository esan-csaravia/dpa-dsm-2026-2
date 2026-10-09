package com.example.sportprog3

import android.app.Application
import com.example.sportprog3.data.remote.FirebaseMatchManager

class SportProApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseMatchManager.enableOfflinePersistence()
    }
}
