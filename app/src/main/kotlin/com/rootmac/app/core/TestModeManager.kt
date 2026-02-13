package com.rootmac.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Manages test mode with countdown timer and auto-revert
 */
object TestModeManager {

    /**
     * Apply MAC in test mode with auto-revert
     */
    suspend fun applyTestMode(
        interfaceName: String,
        newMac: String,
        timeoutSeconds: Int = 60,
        onTick: (secondsRemaining: Int) -> Unit = {},
        onRevert: () -> Unit = {}
    ): TestModeResult = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("Test mode: Applying $newMac to $interfaceName for ${timeoutSeconds}s")
            
            // Get original MAC
            val originalMac = MacAddressManager.getCurrentMac(interfaceName)
            
            // Apply new MAC
            val applyResult = MacAddressManager.changeMacMethodA(interfaceName, newMac)
            
            if (!applyResult.isSuccess) {
                return@withContext TestModeResult(
                    isSuccess = false,
                    message = "Failed to apply test MAC",
                    originalMac = originalMac
                )
            }
            
            // Countdown timer
            for (i in timeoutSeconds downTo 1) {
                onTick(i)
                delay(1000)
            }
            
            // Auto-revert
            Timber.d("Test mode: Auto-reverting to $originalMac")
            val revertResult = MacAddressManager.changeMacMethodA(interfaceName, originalMac)
            onRevert()
            
            TestModeResult(
                isSuccess = revertResult.isSuccess,
                message = if (revertResult.isSuccess) "Test mode completed and reverted" else "Failed to revert MAC",
                originalMac = originalMac
            )
        } catch (e: Exception) {
            Timber.e(e, "Test mode failed")
            TestModeResult(
                isSuccess = false,
                message = "Test mode error: ${e.message}",
                originalMac = ""
            )
        }
    }

    /**
     * Revert on connection failure
     */
    suspend fun revertOnConnectionFailure(
        interfaceName: String,
        originalMac: String,
        checkConnectionFunc: suspend () -> Boolean,
        maxAttempts: Int = 5
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            var attempts = 0
            while (attempts < maxAttempts) {
                delay(2000) // Wait 2 seconds between checks
                
                if (checkConnectionFunc()) {
                    Timber.d("Connection established, test mode successful")
                    return@withContext true
                }
                
                attempts++
                Timber.w("Connection check failed, attempt $attempts/$maxAttempts")
            }
            
            // Connection failed, revert
            Timber.d("Connection failed, reverting to $originalMac")
            val result = MacAddressManager.changeMacMethodA(interfaceName, originalMac)
            result.isSuccess
        } catch (e: Exception) {
            Timber.e(e, "Connection failure check error")
            false
        }
    }
}

data class TestModeResult(
    val isSuccess: Boolean,
    val message: String,
    val originalMac: String
)
