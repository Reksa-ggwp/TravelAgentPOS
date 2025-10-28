package com.travelagent.pos

import android.app.Application
import android.os.StrictMode
import com.dantsu.escposprinter.BuildConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable StrictMode only in debug to keep app lightweight in release
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }
}