package com.rootmac.app.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Random

/**
 * Manages MAC address operations
 */
object MacAddressManager {

    /**
     * Get all network interfaces
     */
    suspend fun getNetworkInterfaces(): List<NetworkInterface> = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = Shell.cmd("ip link show").exec()
            if (result.isSuccess) {
                parseNetworkInterfaces(result.stdout)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get network interfaces")
            emptyList()
        }
    }

    /**
     * Get current MAC address for interface
     */
    suspend fun getCurrentMac(interfaceName: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = Shell.cmd("ip link show $interfaceName").exec()
            if (result.isSuccess) {
                extractMacFromOutput(result.stdout)
            } else {
                ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get MAC for $interfaceName")
            ""
        }
    }

    /**
     * Change MAC address using method A (ip link)
     */
    suspend fun changeMacMethodA(interfaceName: String, newMac: String): CommandResult {
        return withContext(Dispatchers.IO) {
            val commands = listOf(
                "ip link set dev $interfaceName down",
                "ip link set dev $interfaceName address $newMac",
                "ip link set dev $interfaceName up"
            )
            executeSequentially(commands)
        }
    }

    /**
     * Change MAC address using method B (ifconfig)
     */
    suspend fun changeMacMethodB(interfaceName: String, newMac: String): CommandResult {
        return withContext(Dispatchers.IO) {
            val commands = listOf(
                "ifconfig $interfaceName down",
                "ifconfig $interfaceName hw ether $newMac",
                "ifconfig $interfaceName up"
            )
            executeSequentially(commands)
        }
    }

    /**
     * Change MAC address using method C (sysfs)
     */
    suspend fun changeMacMethodC(interfaceName: String, newMac: String): CommandResult {
        return withContext(Dispatchers.IO) {
            val command = "echo $newMac > /sys/class/net/$interfaceName/address"
            RootAccess.executeCommand(command)
        }
    }

    /**
     * Generate random MAC address
     */
    fun generateRandomMac(): String {
        val random = Random()
        val bytes = ByteArray(6)
        random.nextBytes(bytes)
        
        // Set locally administered bit (bit 1 of first octet)
        bytes[0] = (bytes[0].toInt() or 0x02).toByte()
        
        // Clear multicast bit (bit 0 of first octet)
        bytes[0] = (bytes[0].toInt() and 0xFE).toByte()
        
        return bytes.joinToString(":") { "%02x".format(it) }
    }

    /**
     * Validate MAC address format
     */
    fun isValidMac(mac: String): Boolean {
        val macPattern = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
        if (!macPattern.matches(mac)) return false
        
        val bytes = mac.split(":").map { it.toInt(16).toByte() }
        if (bytes.isEmpty()) return false
        
        // Check for multicast MAC (bit 0 of first octet)
        if ((bytes[0].toInt() and 0x01) != 0) return false
        
        // Check for broadcast MAC
        if (bytes.all { it == 0xFF.toByte() }) return false
        
        return true
    }

    /**
     * Normalize MAC address format
     */
    fun normalizeMac(mac: String): String {
        return mac.replace("-", ":").lowercase()
    }

    private suspend fun executeSequentially(commands: List<String>): CommandResult {
        var lastResult = CommandResult(true, "", "")
        val allOutput = StringBuilder()
        val allErrors = StringBuilder()

        for (command in commands) {
            lastResult = RootAccess.executeCommand(command)
            allOutput.append(lastResult.stdout).append("\n")
            allErrors.append(lastResult.stderr).append("\n")
            
            if (!lastResult.isSuccess) {
                break
            }
        }

        return CommandResult(
            isSuccess = lastResult.isSuccess,
            stdout = allOutput.toString(),
            stderr = allErrors.toString()
        )
    }

    private fun parseNetworkInterfaces(output: String): List<NetworkInterface> {
        val interfaces = mutableListOf<NetworkInterface>()
        val lines = output.split("\n")
        
        var currentInterface: NetworkInterface? = null
        
        for (line in lines) {
            if (line.isEmpty()) continue
            
            // New interface line starts with a number
            if (line[0].isDigit()) {
                val parts = line.split(":")
                if (parts.size >= 2) {
                    val name = parts[1].trim().split("<")[0].trim()
                    val state = if (line.contains("UP")) "UP" else "DOWN"
                    
                    currentInterface = NetworkInterface(
                        name = name,
                        state = state,
                        mac = "",
                        ip = ""
                    )
                    interfaces.add(currentInterface)
                }
            } else if (line.contains("link/ether")) {
                // Extract MAC address
                val parts = line.trim().split(" ")
                if (parts.size >= 2) {
                    currentInterface?.mac = parts[1]
                }
            } else if (line.contains("inet ")) {
                // Extract IP address
                val parts = line.trim().split(" ")
                if (parts.size >= 2) {
                    currentInterface?.ip = parts[1].split("/")[0]
                }
            }
        }
        
        return interfaces
    }

    private fun extractMacFromOutput(output: String): String {
        val lines = output.split("\n")
        for (line in lines) {
            if (line.contains("link/ether")) {
                val parts = line.trim().split(" ")
                if (parts.size >= 2) {
                    return parts[1]
                }
            }
        }
        return ""
    }
}

data class NetworkInterface(
    val name: String,
    val state: String,
    var mac: String,
    var ip: String
)
