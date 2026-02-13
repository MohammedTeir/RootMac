package com.rootmac.app.core

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

/**
 * Manages stealth mode features
 */
object StealthModeManager {

    /**
     * Hide launcher icon
     */
    fun hideAppIcon(context: Context): Boolean {
        return try {
            val componentName = ComponentName(context, "com.rootmac.app.MainActivity")
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            Timber.d("App icon hidden")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to hide app icon")
            false
        }
    }

    /**
     * Show launcher icon
     */
    fun showAppIcon(context: Context): Boolean {
        return try {
            val componentName = ComponentName(context, "com.rootmac.app.MainActivity")
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            Timber.d("App icon shown")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to show app icon")
            false
        }
    }

    /**
     * Check if app icon is hidden
     */
    fun isAppIconHidden(context: Context): Boolean {
        return try {
            val componentName = ComponentName(context, "com.rootmac.app.MainActivity")
            val state = context.packageManager.getComponentEnabledSetting(componentName)
            state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Rename app label (requires package manager modification)
     */
    fun renameAppLabel(context: Context, newLabel: String): Boolean {
        return try {
            // This requires modifying package resources at runtime
            // Implementation would depend on system configuration
            Timber.d("App label rename requested: $newLabel")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to rename app label")
            false
        }
    }

    /**
     * Get stealth mode status
     */
    fun getStealthStatus(context: Context): StealthStatus {
        return StealthStatus(
            isIconHidden = isAppIconHidden(context),
            isMinimalFootprint = true // Can be expanded with more checks
        )
    }
}

data class StealthStatus(
    val isIconHidden: Boolean,
    val isMinimalFootprint: Boolean
)
