package com.rootmac.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rootmac.app.worker.NetworkChangeWorker
import timber.log.Timber

class NetworkStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ConnectivityManager.CONNECTIVITY_ACTION,
            "android.net.wifi.STATE_CHANGE",
            "android.net.wifi.WIFI_STATE_CHANGED" -> {
                Timber.d("Network state changed: ${intent.action}")
                
                if (context != null) {
                    val workRequest = OneTimeWorkRequestBuilder<NetworkChangeWorker>().build()
                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "network_change_work",
                        androidx.work.ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }
        }
    }
}
