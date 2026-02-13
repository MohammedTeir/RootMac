package com.rootmac.app.core

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Handles root access validation and command execution
 */
object RootAccess {

    init {
        // Initialize libsu
        Shell.enableVerboseLogging = true
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }

    /**
     * Check if device has root access
     */
    suspend fun isRooted(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Shell.cmd("id").exec().isSuccess
        } catch (e: Exception) {
            Timber.e(e, "Root check failed")
            false
        }
    }

    /**
     * Execute a shell command with root privileges
     */
    suspend fun executeCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = Shell.cmd(command).exec()
            CommandResult(
                isSuccess = result.isSuccess,
                stdout = result.out.joinToString("\n"),
                stderr = result.err.joinToString("\n")
            )
        } catch (e: Exception) {
            Timber.e(e, "Command execution failed: $command")
            CommandResult(
                isSuccess = false,
                stdout = "",
                stderr = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Check if BusyBox is available
     */
    suspend fun hasBusyBox(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Shell.cmd("which busybox").exec().isSuccess
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if iproute2 is available
     */
    suspend fun hasIproute2(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Shell.cmd("which ip").exec().isSuccess
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get SELinux mode
     */
    suspend fun getSELinuxMode(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = Shell.cmd("getenforce").exec()
            if (result.isSuccess) result.out.firstOrNull() ?: "Unknown" else "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

data class CommandResult(
    val isSuccess: Boolean,
    val stdout: String,
    val stderr: String
)
