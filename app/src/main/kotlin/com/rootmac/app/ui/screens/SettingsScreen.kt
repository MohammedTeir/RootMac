package com.rootmac.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rootmac.app.ui.viewmodel.RootMACViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: RootMACViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var autoRestoreOnBoot by remember { mutableStateOf(false) }
    var enablePersistentNotification by remember { mutableStateOf(true) }
    var enableConnectionMonitoring by remember { mutableStateOf(true) }
    var defaultInterface by remember { mutableStateOf(state.selectedInterface ?: "") }
    var defaultMethod by remember { mutableStateOf("A") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge
        )

        // General Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "General",
                    style = MaterialTheme.typography.titleMedium
                )

                SettingRow(
                    label = "Default Interface",
                    value = defaultInterface,
                    onValueChange = { defaultInterface = it },
                    isDropdown = true,
                    options = state.networkInterfaces.map { it.name }
                )

                SettingRow(
                    label = "Default Execution Method",
                    value = defaultMethod,
                    onValueChange = { defaultMethod = it },
                    isDropdown = true,
                    options = listOf("A", "B", "C")
                )
            }
        }

        // Automation Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Automation",
                    style = MaterialTheme.typography.titleMedium
                )

                SettingCheckbox(
                    label = "Auto-restore on boot",
                    checked = autoRestoreOnBoot,
                    onCheckedChange = { autoRestoreOnBoot = it }
                )

                SettingCheckbox(
                    label = "Enable persistent notification",
                    checked = enablePersistentNotification,
                    onCheckedChange = { enablePersistentNotification = it }
                )

                SettingCheckbox(
                    label = "Enable connection monitoring",
                    checked = enableConnectionMonitoring,
                    onCheckedChange = { enableConnectionMonitoring = it }
                )
            }
        }

        // Advanced Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Advanced",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Root Status",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (state.isRooted) "✓ Root access granted" else "✗ Root access denied",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.isRooted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Text(
                    text = "System Information",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )

                InfoRow("BusyBox", if (state.hasBusyBox) "Available" else "Not Found")
                InfoRow("iproute2", if (state.hasIproute2) "Available" else "Not Found")
                InfoRow("SELinux", state.seLinuxMode)
            }
        }

        // About
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )

                InfoRow("App Name", "RootMAC")
                InfoRow("Version", "1.0.0")
                InfoRow("Build", "1")

                Text(
                    text = "Free Root-Only Android MAC Address Changer",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isDropdown: Boolean = false,
    options: List<String> = emptyList()
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        if (isDropdown && options.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                options.forEach { option ->
                    androidx.compose.material3.Button(
                        onClick = { onValueChange(option) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(option)
                    }
                }
            }
        } else {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun SettingCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
