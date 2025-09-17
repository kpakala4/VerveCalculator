package com.example.vervecalculator

import android.app.Application
import android.util.Log
import net.pubnative.lite.sdk.HyBid
import net.pubnative.lite.sdk.analytics.ReportingController

class HybridApp : Application() {
    override fun onCreate() {
        super.onCreate()
        HyBid.initialize(
            this,
            APP_TOKEN,
            object : HyBid.InitialisationListener {
                override fun onInitialisationFinished(success: Boolean) {
                    Log.d(TAG, "HyBid initialized: $success")
                }
            },
            ReportingController.ReportInitializationMode.APP_START
        )
        HyBid.setTestMode(true)
    }

    companion object {
        private const val TAG = "HybridApp"
        const val APP_TOKEN = "dde3c298b47648459f8ada4a982fa92d"
        const val ZONE_ID = "1"
    }
}
