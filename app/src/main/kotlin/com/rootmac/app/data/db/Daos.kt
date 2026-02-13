package com.rootmac.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MacProfileDao {
    @Query("SELECT * FROM mac_profiles ORDER BY updatedAt DESC")
    fun getAllProfiles(): Flow<List<MacProfileEntity>>

    @Query("SELECT * FROM mac_profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): MacProfileEntity?

    @Insert
    suspend fun insertProfile(profile: MacProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: MacProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: MacProfileEntity)

    @Query("SELECT * FROM mac_profiles WHERE autoApply = 1")
    suspend fun getAutoApplyProfiles(): List<MacProfileEntity>
}

@Dao
interface MacLogDao {
    @Query("SELECT * FROM mac_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<MacLogEntity>>

    @Query("SELECT * FROM mac_logs WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getLogsForProfile(profileId: Int): Flow<List<MacLogEntity>>

    @Insert
    suspend fun insertLog(log: MacLogEntity)

    @Query("DELETE FROM mac_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM mac_logs WHERE timestamp < :beforeTime")
    suspend fun clearOldLogs(beforeTime: Long)
}

@Dao
interface OriginalMacDao {
    @Query("SELECT * FROM original_macs WHERE interfaceName = :interfaceName")
    suspend fun getOriginalMac(interfaceName: String): OriginalMacEntity?

    @Query("SELECT * FROM original_macs")
    suspend fun getAllOriginalMacs(): List<OriginalMacEntity>

    @Insert
    suspend fun insertOriginalMac(mac: OriginalMacEntity)

    @Update
    suspend fun updateOriginalMac(mac: OriginalMacEntity)

    @Delete
    suspend fun deleteOriginalMac(mac: OriginalMacEntity)
}

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?

    @Insert
    suspend fun insertSetting(setting: AppSettingEntity)

    @Update
    suspend fun updateSetting(setting: AppSettingEntity)

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun deleteSetting(key: String)
}
