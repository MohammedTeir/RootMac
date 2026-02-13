package com.rootmac.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootmac.app.core.MacAddressManager
import com.rootmac.app.core.NetworkInterface
import com.rootmac.app.core.RootAccess
import com.rootmac.app.data.Repository
import com.rootmac.app.data.db.MacLogEntity
import com.rootmac.app.data.db.MacProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class RootMACState(
    val isRooted: Boolean = false,
    val hasBusyBox: Boolean = false,
    val hasIproute2: Boolean = false,
    val seLinuxMode: String = "Unknown",
    val networkInterfaces: List<NetworkInterface> = emptyList(),
    val profiles: List<MacProfileEntity> = emptyList(),
    val recentLogs: List<MacLogEntity> = emptyList(),
    val selectedInterface: String? = null,
    val currentMac: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RootMACViewModel(context: Context) : ViewModel() {
    private val repository = Repository(context)

    private val _state = MutableStateFlow(RootMACState())
    val state: StateFlow<RootMACState> = _state.asStateFlow()

    init {
        checkRootAccess()
        loadProfiles()
        loadRecentLogs()
        refreshNetworkInterfaces()
    }

    private fun checkRootAccess() {
        viewModelScope.launch {
            try {
                val isRooted = RootAccess.isRooted()
                val hasBusyBox = RootAccess.hasBusyBox()
                val hasIproute2 = RootAccess.hasIproute2()
                val seLinuxMode = RootAccess.getSELinuxMode()

                _state.value = _state.value.copy(
                    isRooted = isRooted,
                    hasBusyBox = hasBusyBox,
                    hasIproute2 = hasIproute2,
                    seLinuxMode = seLinuxMode
                )

                if (!isRooted) {
                    _state.value = _state.value.copy(
                        errorMessage = "Root access denied. This app requires a rooted device."
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking root access")
                _state.value = _state.value.copy(
                    errorMessage = "Error checking root access: ${e.message}"
                )
            }
        }
    }

    fun refreshNetworkInterfaces() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val interfaces = MacAddressManager.getNetworkInterfaces()
                _state.value = _state.value.copy(
                    networkInterfaces = interfaces,
                    isLoading = false
                )

                if (interfaces.isNotEmpty()) {
                    selectInterface(interfaces[0].name)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing network interfaces")
                _state.value = _state.value.copy(
                    errorMessage = "Error refreshing interfaces: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun selectInterface(interfaceName: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(selectedInterface = interfaceName)
                val currentMac = MacAddressManager.getCurrentMac(interfaceName)
                _state.value = _state.value.copy(currentMac = currentMac)
            } catch (e: Exception) {
                Timber.e(e, "Error selecting interface")
            }
        }
    }

    fun changeMAC(interfaceName: String, newMac: String, method: String = "A") {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                // Validate MAC
                if (!MacAddressManager.isValidMac(newMac)) {
                    _state.value = _state.value.copy(
                        errorMessage = "Invalid MAC address format",
                        isLoading = false
                    )
                    return@launch
                }

                val normalizedMac = MacAddressManager.normalizeMac(newMac)
                val oldMac = MacAddressManager.getCurrentMac(interfaceName)

                // Save original MAC if not already saved
                val originalMac = repository.getOriginalMac(interfaceName)
                if (originalMac == null) {
                    repository.saveOriginalMac(interfaceName, oldMac)
                }

                // Execute MAC change
                val startTime = System.currentTimeMillis()
                val result = when (method) {
                    "B" -> MacAddressManager.changeMacMethodB(interfaceName, normalizedMac)
                    "C" -> MacAddressManager.changeMacMethodC(interfaceName, normalizedMac)
                    else -> MacAddressManager.changeMacMethodA(interfaceName, normalizedMac)
                }
                val executionTime = System.currentTimeMillis() - startTime

                // Log the operation
                repository.insertLog(
                    MacLogEntity(
                        interfaceName = interfaceName,
                        oldMac = oldMac,
                        newMac = normalizedMac,
                        isSuccess = result.isSuccess,
                        executionTime = executionTime,
                        stdout = result.stdout,
                        stderr = result.stderr
                    )
                )

                if (result.isSuccess) {
                    _state.value = _state.value.copy(
                        currentMac = normalizedMac,
                        errorMessage = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        errorMessage = "Failed to change MAC: ${result.stderr}"
                    )
                }

                _state.value = _state.value.copy(isLoading = false)
                loadRecentLogs()
            } catch (e: Exception) {
                Timber.e(e, "Error changing MAC")
                _state.value = _state.value.copy(
                    errorMessage = "Error: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun generateRandomMAC() {
        val randomMac = MacAddressManager.generateRandomMac()
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun restoreOriginalMAC(interfaceName: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val originalMac = repository.getOriginalMac(interfaceName)

                if (originalMac != null) {
                    changeMAC(interfaceName, originalMac.originalMac)
                } else {
                    _state.value = _state.value.copy(
                        errorMessage = "No original MAC saved for this interface",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error restoring original MAC")
                _state.value = _state.value.copy(
                    errorMessage = "Error: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            repository.getAllProfiles().collect { profiles ->
                _state.value = _state.value.copy(profiles = profiles)
            }
        }
    }

    private fun loadRecentLogs() {
        viewModelScope.launch {
            repository.getRecentLogs(50).collect { logs ->
                _state.value = _state.value.copy(recentLogs = logs)
            }
        }
    }

    suspend fun createProfile(profile: MacProfileEntity) {
        repository.insertProfile(profile)
    }

    suspend fun updateProfile(profile: MacProfileEntity) {
        repository.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: MacProfileEntity) {
        repository.deleteProfile(profile)
    }
}
