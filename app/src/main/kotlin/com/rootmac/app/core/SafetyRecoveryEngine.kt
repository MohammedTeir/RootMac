package com.rootmac.app.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Safety and recovery mechanisms for MAC spoofing
 */
object SafetyRecoveryEngine {

    private const val NOTIFICATION_CHANNEL_ID = "rootmac_spoofing"
    private const val NOTIFICATION_ID = 1001

    /**
     * Show persistent notification when spoofing is active
     */
    fun showSpoofingNotification(
        context: Context,
        interfaceName: String,
        currentMac: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel for Android 8+
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "RootMAC Spoofing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when MAC spoofing is active"
            }
            notificationManager.createNotificationChannel(channel)
            
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("MAC Spoofing Active")
                .setContentText("$interfaceName: $currentMac")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            Timber.d("Spoofing notification shown")
        } catch (e: Exception) {
            Timber.e(e, "Failed to show spoofing notification")
        }
    }

    /**
     * Hide spoofing notification
     */
    fun hideSpoofingNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
            Timber.d("Spoofing notification hidden")
        } catch (e: Exception) {
            Timber.e(e, "Failed to hide spoofing notification")
        }
    }

    /**
     * Monitor connection status
     */
    suspend fun monitorConnection(
        interfaceName: String,
        checkInterval: Long = 5000,
        maxDuration: Long = 300000 // 5 minutes
    ): ConnectionMonitorResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val startTime = System.currentTimeMillis()
            var lastCheckTime = startTime
            var isConnected = true
            
            while (System.currentTimeMillis() - startTime < maxDuration) {
                val currentTime = System.currentTimeMillis()
                
                if (currentTime - lastCheckTime >= checkInterval) {
                    // Check if interface is still up
                    val result = RootAccess.executeCommand("ip link show $interfaceName")
                    isConnected = result.isSuccess && result.stdout.contains("UP")
                    
                    if (!isConnected) {
                        Timber.w("Connection lost on $interfaceName")
                        return@withContext ConnectionMonitorResult(
                            isConnected = false,
                            duration = currentTime - startTime,
                            failurePoint = currentTime - startTime
                        )
                    }
                    
                    lastCheckTime = currentTime
                }
                
                kotlinx.coroutines.delay(100)
            }
            
            ConnectionMonitorResult(
                isConnected = true,
                duration = System.currentTimeMillis() - startTime,
                failurePoint = -1
            )
        } catch (e: Exception) {
            Timber.e(e, "Connection monitoring error")
            ConnectionMonitorResult(
                isConnected = false,
                duration = 0,
                failurePoint = 0
            )
        }
    }

    /**
     * Crash-safe restore on next boot
     */
    suspend fun setupCrashSafeRestore(
        context: Context,
        interfaceName: String,
        originalMac: String
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // Save to shared preferences for crash recovery
            val prefs = context.getSharedPreferences("crash_recovery", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("interface_$interfaceName", originalMac)
                putLong("timestamp", System.currentTimeMillis())
                apply()
            }
            Timber.d("Crash-safe restore configured for $interfaceName")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to setup crash-safe restore")
            false
        }
    }

    /**
     * Restore from crash recovery
     */
    suspend fun restoreFromCrash(context: Context): List<CrashRecoveryItem> = withContext(Dispatchers.IO) {
        return@withContext try {
            val prefs = context.getSharedPreferences("crash_recovery", Context.MODE_PRIVATE)
            val items = mutableListOf<CrashRecoveryItem>()
            
            for ((key, value) in prefs.all) {
                if (key.startsWith("interface_") && value is String) {
                    val interfaceName = key.removePrefix("interface_")
                    items.add(CrashRecoveryItem(interfaceName, value))
                }
            }
            
            if (items.isNotEmpty()) {
                Timber.d("Found ${items.size} items for crash recovery")
            }
            
            items
        } catch (e: Exception) {
            Timber.e(e, "Failed to restore from crash")
            emptyList()
        }
    }

    /**
     * Detect driver MAC locking
     */
    suspend fun detectDriverMacLocking(interfaceName: String): DriverLockingResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val originalMac = MacAddressManager.getCurrentMac(interfaceName)
            
            // Try to change MAC
            val testMac = AdvancedMacGenerator.generateRealistic()
            val changeResult = MacAddressManager.changeMacMethodA(interfaceName, testMac)
            
            if (!changeResult.isSuccess) {
                return@withContext DriverLockingResult(
                    isLocked = true,
                    reason = "MAC change command failed"
                )
            }
            
            // Check if MAC actually changed
            val newMac = MacAddressManager.getCurrentMac(interfaceName)
            
            // Restore original
            MacAddressManager.changeMacMethodA(interfaceName, originalMac)
            
            if (newMac == originalMac) {
                return@withContext DriverLockingResult(
                    isLocked = true,
                    reason = "MAC did not change (driver locked)"
                )
            }
            
            DriverLockingResult(
                isLocked = false,
                reason = "MAC change successful"
            )
        } catch (e: Exception) {
            Timber.e(e, "Driver locking detection error")
            DriverLockingResult(
                isLocked = true,
                reason = "Detection error: ${e.message}"
            )
        }
    }

    /**
     * Detect reset behavior after reboot
     */
    suspend fun detectResetBehavior(interfaceName: String): ResetBehaviorResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentMac = MacAddressManager.getCurrentMac(interfaceName)
            
            // This would require actual reboot detection
            // For now, return unknown
            ResetBehaviorResult(
                resetsOnReboot = null,
                reason = "Requires reboot detection"
            )
        } catch (e: Exception) {
            Timber.e(e, "Reset behavior detection error")
            ResetBehaviorResult(
                resetsOnReboot = null,
                reason = "Detection error: ${e.message}"
            )
        }
    }

    /**
     * Calculate compatibility score
     */
    suspend fun calculateCompatibilityScore(): Int = withContext(Dispatchers.IO) {
        return@withContext try {
            var score = 0
            
            // Root access
            if (RootAccess.isRooted()) score += 20
            
            // BusyBox
            if (RootAccess.hasBusyBox()) score += 20
            
            // iproute2
            if (RootAccess.hasIproute2()) score += 20
            
            // SELinux mode
            val seLinuxMode = RootAccess.getSELinuxMode()
            if (seLinuxMode.contains("Permissive", ignoreCase = true)) score += 20
            
            // Default score
            score += 20
            
            score.coerceIn(0, 100)
        } catch (e: Exception) {
            Timber.e(e, "Compatibility score calculation error")
            0
        }
    }
}

data class ConnectionMonitorResult(
    val isConnected: Boolean,
    val duration: Long,
    val failurePoint: Long
)

data class CrashRecoveryItem(
    val interfaceName: String,
    val originalMac: String
)

data class DriverLockingResult(
    val isLocked: Boolean,
    val reason: String
)

data class ResetBehaviorResult(
    val resetsOnReboot: Boolean?,
    val reason: String
)
