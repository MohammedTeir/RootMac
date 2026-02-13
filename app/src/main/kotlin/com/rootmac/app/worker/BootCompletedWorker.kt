package com.rootmac.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rootmac.app.core.MacAddressManager
import com.rootmac.app.data.Repository
import timber.log.Timber

class BootCompletedWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("BootCompletedWorker: Starting")
            
            val repository = Repository(applicationContext)
            val autoApplyProfiles = repository.getAutoApplyProfiles()
            
            Timber.d("Found ${autoApplyProfiles.size} auto-apply profiles")
            
            // Apply each auto-apply profile
            for (profile in autoApplyProfiles) {
                try {
                    Timber.d("Applying profile: ${profile.name}")
                    
                    val startTime = System.currentTimeMillis()
                    val result = when (profile.executionMethod) {
                        "B" -> MacAddressManager.changeMacMethodB(profile.interfaceName, profile.macAddress)
                        "C" -> MacAddressManager.changeMacMethodC(profile.interfaceName, profile.macAddress)
                        else -> MacAddressManager.changeMacMethodA(profile.interfaceName, profile.macAddress)
                    }
                    val executionTime = System.currentTimeMillis() - startTime
                    
                    // Log the operation
                    val oldMac = MacAddressManager.getCurrentMac(profile.interfaceName)
                    repository.insertLog(
                        com.rootmac.app.data.db.MacLogEntity(
                            profileId = profile.id,
                            interfaceName = profile.interfaceName,
                            oldMac = oldMac,
                            newMac = profile.macAddress,
                            isSuccess = result.isSuccess,
                            executionTime = executionTime,
                            stdout = result.stdout,
                            stderr = result.stderr
                        )
                    )
                    
                    Timber.d("Profile applied: ${profile.name}, success: ${result.isSuccess}")
                } catch (e: Exception) {
                    Timber.e(e, "Error applying profile: ${profile.name}")
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "BootCompletedWorker failed")
            Result.retry()
        }
    }
}
