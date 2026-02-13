package com.rootmac.app.core

import java.util.Random

/**
 * Advanced MAC address generation with OUI support and realism
 */
object AdvancedMacGenerator {

    // Common vendor OUI prefixes (first 3 octets)
    private val COMMON_OUIS = listOf(
        "00:1A:2B",  // Intel
        "00:25:86",  // Apple
        "00:50:F2",  // Microsoft
        "08:00:27",  // QEMU
        "52:54:00",  // QEMU/KVM
        "00:0C:29",  // VMware
        "00:05:69",  // VMware
        "00:16:3E",  // Xen
        "00:1C:42",  // Parallels
        "00:21:F5",  // Dell
        "00:23:45",  // Cisco
        "00:26:99",  // Huawei
        "00:1F:3C",  // Broadcom
        "00:11:22",  // Linksys
        "00:13:10",  // Belkin
        "00:14:6C",  // Netgear
        "00:15:E9",  // TP-Link
        "00:17:3F",  // Asus
        "00:19:E0",  // D-Link
        "00:1B:11",  // Atheros
    )

    /**
     * Generate random MAC with valid OUI (vendor prefix)
     */
    fun generateWithValidOUI(): String {
        val random = Random()
        val oui = COMMON_OUIS[random.nextInt(COMMON_OUIS.size)]
        val bytes = ByteArray(3)
        random.nextBytes(bytes)
        
        return "$oui:${"%02x".format(bytes[0])}:${"%02x".format(bytes[1])}:${"%02x".format(bytes[2])}"
    }

    /**
     * Clone connected network BSSID (requires WiFi info)
     */
    suspend fun cloneConnectedBSSID(): String? {
        return try {
            // This would require WiFi manager access
            // For now, return null - implement with actual WiFi manager
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Match device manufacturer OUI
     */
    fun matchDeviceManufacturerOUI(deviceOUI: String): String {
        val random = Random()
        val bytes = ByteArray(3)
        random.nextBytes(bytes)
        
        return "$deviceOUI:${"%02x".format(bytes[0])}:${"%02x".format(bytes[1])}:${"%02x".format(bytes[2])}"
    }

    /**
     * Generate MAC with optional locally administered bit control
     */
    fun generateWithLocalBitControl(useLocalBit: Boolean = true): String {
        val random = Random()
        val bytes = ByteArray(6)
        random.nextBytes(bytes)
        
        // Control locally administered bit (bit 1 of first octet)
        if (useLocalBit) {
            bytes[0] = (bytes[0].toInt() or 0x02).toByte()
        } else {
            bytes[0] = (bytes[0].toInt() and 0xFD).toByte()
        }
        
        // Clear multicast bit (bit 0 of first octet)
        bytes[0] = (bytes[0].toInt() and 0xFE).toByte()
        
        return bytes.joinToString(":") { "%02x".format(it) }
    }

    /**
     * Generate realistic MAC by combining OUI with random suffix
     */
    fun generateRealistic(): String {
        val random = Random()
        val oui = COMMON_OUIS[random.nextInt(COMMON_OUIS.size)]
        val suffix = (0..2).joinToString(":") { "%02x".format(random.nextInt(256)) }
        return "$oui:$suffix"
    }

    /**
     * Analyze MAC address and return info
     */
    fun analyzeMac(mac: String): MacInfo {
        val normalized = mac.replace("-", ":").lowercase()
        val bytes = normalized.split(":").map { it.toInt(16).toByte() }
        
        if (bytes.size != 6) {
            return MacInfo(
                isValid = false,
                isUnicast = false,
                isGlobal = false,
                isLocallyAdministered = false,
                vendor = "Unknown"
            )
        }
        
        val firstByte = bytes[0].toInt() and 0xFF
        val isUnicast = (firstByte and 0x01) == 0
        val isGlobal = (firstByte and 0x02) == 0
        val isLocallyAdministered = (firstByte and 0x02) != 0
        
        val oui = "${"%02x".format(bytes[0])}:${"%02x".format(bytes[1])}:${"%02x".format(bytes[2])}"
        val vendor = getVendorName(oui)
        
        return MacInfo(
            isValid = true,
            isUnicast = isUnicast,
            isGlobal = isGlobal,
            isLocallyAdministered = isLocallyAdministered,
            vendor = vendor
        )
    }

    private fun getVendorName(oui: String): String {
        return when (oui.lowercase()) {
            "00:1a:2b" -> "Intel"
            "00:25:86" -> "Apple"
            "00:50:f2" -> "Microsoft"
            "08:00:27" -> "QEMU"
            "52:54:00" -> "QEMU/KVM"
            "00:0c:29" -> "VMware"
            "00:05:69" -> "VMware"
            "00:16:3e" -> "Xen"
            "00:1c:42" -> "Parallels"
            "00:21:f5" -> "Dell"
            "00:23:45" -> "Cisco"
            "00:26:99" -> "Huawei"
            "00:1f:3c" -> "Broadcom"
            "00:11:22" -> "Linksys"
            "00:13:10" -> "Belkin"
            "00:14:6c" -> "Netgear"
            "00:15:e9" -> "TP-Link"
            "00:17:3f" -> "Asus"
            "00:19:e0" -> "D-Link"
            "00:1b:11" -> "Atheros"
            else -> "Unknown"
        }
    }
}

data class MacInfo(
    val isValid: Boolean,
    val isUnicast: Boolean,
    val isGlobal: Boolean,
    val isLocallyAdministered: Boolean,
    val vendor: String
)
