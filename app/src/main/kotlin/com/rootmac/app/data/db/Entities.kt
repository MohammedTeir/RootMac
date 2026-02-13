package com.rootmac.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "mac_profiles")
data class MacProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val interfaceName: String,
    val macAddress: String,
    val executionMethod: String, // A, B, or C
    val autoApply: Boolean = false,
    val ssidTarget: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mac_logs")
data class MacLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profileId: Int? = null,
    val interfaceName: String,
    val oldMac: String,
    val newMac: String,
    val isSuccess: Boolean,
    val executionTime: Long, // in milliseconds
    val stdout: String = "",
    val stderr: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "original_macs")
data class OriginalMacEntity(
    @PrimaryKey
    val interfaceName: String,
    val originalMac: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
