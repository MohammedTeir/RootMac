package com.rootmac.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.rootmac.app.ui.viewmodel.RootMACViewModel

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel = remember { RootMACViewModel(context) }
    val state by viewModel.state.collectAsState()

    var newMacInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("A") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RootMAC",
                style = MaterialTheme.typography.headlineLarge
            )
            IconButton(onClick = { viewModel.refreshNetworkInterfaces() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        // Root Status Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "System Status",
                    style = MaterialTheme.typography.titleMedium
                )
                StatusRow("Root Access", if (state.isRooted) "✓ Granted" else "✗ Denied")
                StatusRow("BusyBox", if (state.hasBusyBox) "✓ Available" else "✗ Not Found")
                StatusRow("iproute2", if (state.hasIproute2) "✓ Available" else "✗ Not Found")
                StatusRow("SELinux", state.seLinuxMode)
            }
        }

        // Error Message
        if (state.errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = state.errorMessage!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Interface Selection
        if (state.networkInterfaces.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Network Interfaces",
                        style = MaterialTheme.typography.titleMedium
                    )
                    state.networkInterfaces.forEach { interface ->
                        Button(
                            onClick = { viewModel.selectInterface(interface.name) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text("${interface.name} (${interface.state})")
                                Text(
                                    text = interface.mac,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (interface.ip.isNotEmpty()) {
                                    Text(
                                        text = interface.ip,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Current MAC Display
        if (state.selectedInterface != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Current MAC: ${state.selectedInterface}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = state.currentMac,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // MAC Change Controls
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Change MAC Address",
                        style = MaterialTheme.typography.titleMedium
                    )

                    TextField(
                        value = newMacInput,
                        onValueChange = { newMacInput = it },
                        label = { Text("New MAC Address") },
                        placeholder = { Text("XX:XX:XX:XX:XX:XX") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("A", "B", "C").forEach { method ->
                            Button(
                                onClick = { selectedMethod = method },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Method $method")
                            }
                        }
                    }

                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (state.selectedInterface != null && newMacInput.isNotEmpty()) {
                                    viewModel.changeMAC(
                                        state.selectedInterface!!,
                                        newMacInput,
                                        selectedMethod
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply MAC Change")
                        }

                        Button(
                            onClick = {
                                if (state.selectedInterface != null) {
                                    viewModel.restoreOriginalMAC(state.selectedInterface!!)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Restore Original MAC")
                        }

                        Button(
                            onClick = {
                                newMacInput = MacAddressManager.generateRandomMac()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Random MAC")
                        }
                    }
                }
            }
        }

        // Recent Logs
        if (state.recentLogs.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recent Logs",
                        style = MaterialTheme.typography.titleMedium
                    )
                    state.recentLogs.take(5).forEach { log ->
                        Text(
                            text = "${log.interfaceName}: ${log.oldMac} → ${log.newMac}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (log.isSuccess) "✓ Success" else "✗ Failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (log.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

// Import for MacAddressManager
import com.rootmac.app.core.MacAddressManager
