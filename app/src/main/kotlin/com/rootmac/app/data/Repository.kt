package com.rootmac.app.data

import android.content.Context
import com.rootmac.app.data.db.AppDatabase
import com.rootmac.app.data.db.MacLogEntity
import com.rootmac.app.data.db.MacProfileEntity
import com.rootmac.app.data.db.OriginalMacEntity
import kotlinx.coroutines.flow.Flow

class Repository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val profileDao = database.macProfileDao()
    private val logDao = database.macLogDao()
    private val originalMacDao = database.originalMacDao()
    private val settingDao = database.appSettingDao()

    // Profile operations
    fun getAllProfiles(): Flow<List<MacProfileEntity>> = profileDao.getAllProfiles()

    suspend fun getProfileById(id: Int): MacProfileEntity? = profileDao.getProfileById(id)

    suspend fun insertProfile(profile: MacProfileEntity): Long = profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: MacProfileEntity) = profileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: MacProfileEntity) = profileDao.deleteProfile(profile)

    suspend fun getAutoApplyProfiles(): List<MacProfileEntity> = profileDao.getAutoApplyProfiles()

    // Log operations
    fun getRecentLogs(limit: Int = 100): Flow<List<MacLogEntity>> = logDao.getRecentLogs(limit)

    fun getLogsForProfile(profileId: Int): Flow<List<MacLogEntity>> = logDao.getLogsForProfile(profileId)

    suspend fun insertLog(log: MacLogEntity) = logDao.insertLog(log)

    suspend fun clearAllLogs() = logDao.clearAllLogs()

    suspend fun clearOldLogs(beforeTime: Long) = logDao.clearOldLogs(beforeTime)

    // Original MAC operations
    suspend fun getOriginalMac(interfaceName: String): OriginalMacEntity? =
        originalMacDao.getOriginalMac(interfaceName)

    suspend fun getAllOriginalMacs(): List<OriginalMacEntity> = originalMacDao.getAllOriginalMacs()

    suspend fun saveOriginalMac(interfaceName: String, mac: String) {
        val existing = originalMacDao.getOriginalMac(interfaceName)
        if (existing != null) {
            originalMacDao.updateOriginalMac(existing.copy(originalMac = mac))
        } else {
            originalMacDao.insertOriginalMac(OriginalMacEntity(interfaceName, mac))
        }
    }

    suspend fun deleteOriginalMac(interfaceName: String) {
        val mac = originalMacDao.getOriginalMac(interfaceName)
        if (mac != null) {
            originalMacDao.deleteOriginalMac(mac)
        }
    }

    // Settings operations
    suspend fun getSetting(key: String): String? = settingDao.getSettingValue(key)

    suspend fun setSetting(key: String, value: String) {
        val existing = settingDao.getSetting(key)
        if (existing != null) {
            settingDao.updateSetting(existing.copy(value = value))
        } else {
            settingDao.insertSetting(com.rootmac.app.data.db.AppSettingEntity(key, value))
        }
    }
}
