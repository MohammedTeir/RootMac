package com.rootmac.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rootmac.app.worker.BootCompletedWorker
import timber.log.Timber

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Boot completed received")
            
            if (context != null) {
                val workRequest = OneTimeWorkRequestBuilder<BootCompletedWorker>().build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "boot_completed_work",
                    androidx.work.ExistingWorkPolicy.KEEP,
                    workRequest
                )
            }
        }
    }
}
