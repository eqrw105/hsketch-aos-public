package com.nims.hsketch

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener

class AdApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, OnInitializationCompleteListener {
            AdOpenManager(this)
        })
    }
}