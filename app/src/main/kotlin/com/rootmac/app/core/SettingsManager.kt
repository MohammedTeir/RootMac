package com.rootmac.app.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber

/**
 * Manages app settings with encryption
 */
object SettingsManager {

    private const val SETTINGS_FILE = "rootmac_settings"

    private fun getEncryptedPreferences(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            SETTINGS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Timber.e(e, "Failed to create encrypted preferences, falling back to regular")
        context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
    }

    // Setting keys
    object Keys {
        const val DEFAULT_INTERFACE = "default_interface"
        const val DEFAULT_METHOD = "default_method"
        const val AUTO_RESTORE_ON_BOOT = "auto_restore_on_boot"
        const val ENABLE_PERSISTENT_NOTIFICATION = "enable_persistent_notification"
        const val ENABLE_CONNECTION_MONITORING = "enable_connection_monitoring"
        const val ALLOW_LOCALLY_ADMINISTERED_BIT = "allow_locally_administered_bit"
        const val STEALTH_MODE_ENABLED = "stealth_mode_enabled"
        const val HIDE_LAUNCHER_ICON = "hide_launcher_icon"
        const val LAST_USED_MAC = "last_used_mac"
        const val LAST_USED_INTERFACE = "last_used_interface"
        const val COMPATIBILITY_SCORE = "compatibility_score"
    }

    // Getters
    fun getDefaultInterface(context: Context): String {
        return getEncryptedPreferences(context).getString(Keys.DEFAULT_INTERFACE, "wlan0") ?: "wlan0"
    }

    fun getDefaultMethod(context: Context): String {
        return getEncryptedPreferences(context).getString(Keys.DEFAULT_METHOD, "A") ?: "A"
    }

    fun isAutoRestoreOnBoot(context: Context): Boolean {
        return getEncryptedPreferences(context).getBoolean(Keys.AUTO_RESTORE_ON_BOOT, false)
    }

    fun isPersistentNotificationEnabled(context: Context): Boolean {
        return getEncryptedPreferences(context).getBoolean(Keys.ENABLE_PERSISTENT_NOTIFICATION, true)
    }

    fun isConnectionMonitoringEnabled(context: Context): Boolean {
        return getEncryptedPreferences(context).getBoolean(Keys.ENABLE_CONNECTION_MONITORING, true)
    }

    fun isLocallyAdministeredBitAllowed(context: Context): Boolean {
        return getEncryptedPreferences(context).getBoolean(Keys.ALLOW_LOCALLY_ADMINISTERED_BIT, true)
    }

    fun isStealthModeEnabled(context: Context): Boolean {
        return getEncryptedPreferences(context).getBoolean(Keys.STEALTH_MODE_ENABLED, false)
    }

    fun isLauncherIconHidden(context: Context): Boolean {
        return getEncryptedPreferences(context).getBoolean(Keys.HIDE_LAUNCHER_ICON, false)
    }

    fun getLastUsedMac(context: Context): String {
        return getEncryptedPreferences(context).getString(Keys.LAST_USED_MAC, "") ?: ""
    }

    fun getLastUsedInterface(context: Context): String {
        return getEncryptedPreferences(context).getString(Keys.LAST_USED_INTERFACE, "wlan0") ?: "wlan0"
    }

    fun getCompatibilityScore(context: Context): Int {
        return getEncryptedPreferences(context).getInt(Keys.COMPATIBILITY_SCORE, 0)
    }

    // Setters
    fun setDefaultInterface(context: Context, interface: String) {
        getEncryptedPreferences(context).edit().putString(Keys.DEFAULT_INTERFACE, interface).apply()
    }

    fun setDefaultMethod(context: Context, method: String) {
        getEncryptedPreferences(context).edit().putString(Keys.DEFAULT_METHOD, method).apply()
    }

    fun setAutoRestoreOnBoot(context: Context, enabled: Boolean) {
        getEncryptedPreferences(context).edit().putBoolean(Keys.AUTO_RESTORE_ON_BOOT, enabled).apply()
    }

    fun setPersistentNotificationEnabled(context: Context, enabled: Boolean) {
        getEncryptedPreferences(context).edit().putBoolean(Keys.ENABLE_PERSISTENT_NOTIFICATION, enabled).apply()
    }

    fun setConnectionMonitoringEnabled(context: Context, enabled: Boolean) {
        getEncryptedPreferences(context).edit().putBoolean(Keys.ENABLE_CONNECTION_MONITORING, enabled).apply()
    }

    fun setLocallyAdministeredBitAllowed(context: Context, allowed: Boolean) {
        getEncryptedPreferences(context).edit().putBoolean(Keys.ALLOW_LOCALLY_ADMINISTERED_BIT, allowed).apply()
    }

    fun setStealthModeEnabled(context: Context, enabled: Boolean) {
        getEncryptedPreferences(context).edit().putBoolean(Keys.STEALTH_MODE_ENABLED, enabled).apply()
    }

    fun setLauncherIconHidden(context: Context, hidden: Boolean) {
        getEncryptedPreferences(context).edit().putBoolean(Keys.HIDE_LAUNCHER_ICON, hidden).apply()
    }

    fun setLastUsedMac(context: Context, mac: String) {
        getEncryptedPreferences(context).edit().putString(Keys.LAST_USED_MAC, mac).apply()
    }

    fun setLastUsedInterface(context: Context, interface: String) {
        getEncryptedPreferences(context).edit().putString(Keys.LAST_USED_INTERFACE, interface).apply()
    }

    fun setCompatibilityScore(context: Context, score: Int) {
        getEncryptedPreferences(context).edit().putInt(Keys.COMPATIBILITY_SCORE, score).apply()
    }

    // Clear all settings
    fun clearAllSettings(context: Context) {
        getEncryptedPreferences(context).edit().clear().apply()
        Timber.d("All settings cleared")
    }
}
