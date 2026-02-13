package com.rootmac.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rootmac.app.data.db.MacProfileEntity
import com.rootmac.app.ui.viewmodel.RootMACViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfilesScreen(viewModel: RootMACViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<MacProfileEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "MAC Profiles",
            style = MaterialTheme.typography.headlineLarge
        )

        if (state.profiles.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No profiles yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Create a profile to automate MAC changes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onEdit = { editingProfile = it },
                        onDelete = { scope.launch { viewModel.deleteProfile(it) } },
                        onApply = { scope.launch { viewModel.changeMAC(profile.interfaceName, profile.macAddress, profile.executionMethod) } }
                    )
                }
            }
        }

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Text("Create Profile")
        }
    }

    if (showCreateDialog) {
        ProfileDialog(
            profile = null,
            interfaces = state.networkInterfaces.map { it.name },
            onSave = { profile ->
                scope.launch {
                    viewModel.createProfile(profile)
                    showCreateDialog = false
                }
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (editingProfile != null) {
        ProfileDialog(
            profile = editingProfile,
            interfaces = state.networkInterfaces.map { it.name },
            onSave = { profile ->
                scope.launch {
                    viewModel.updateProfile(profile)
                    editingProfile = null
                }
            },
            onDismiss = { editingProfile = null }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: MacProfileEntity,
    onEdit: (MacProfileEntity) -> Unit,
    onDelete: (MacProfileEntity) -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${profile.interfaceName} - ${profile.macAddress}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    IconButton(onClick = { onEdit(profile) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(profile) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto-apply:", style = MaterialTheme.typography.bodySmall)
                Checkbox(
                    checked = profile.autoApply,
                    onCheckedChange = { }
                )
                Text("Method ${profile.executionMethod}", style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Profile")
            }
        }
    }
}

@Composable
private fun ProfileDialog(
    profile: MacProfileEntity?,
    interfaces: List<String>,
    onSave: (MacProfileEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var selectedInterface by remember { mutableStateOf(profile?.interfaceName ?: (interfaces.firstOrNull() ?: "")) }
    var macAddress by remember { mutableStateOf(profile?.macAddress ?: "") }
    var executionMethod by remember { mutableStateOf(profile?.executionMethod ?: "A") }
    var autoApply by remember { mutableStateOf(profile?.autoApply ?: false) }
    var ssidTarget by remember { mutableStateOf(profile?.ssidTarget ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (profile == null) "Create Profile" else "Edit Profile",
            style = MaterialTheme.typography.headlineSmall
        )

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Profile Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Interface selection
        Text("Interface:", style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            interfaces.forEach { iface ->
                Button(
                    onClick = { selectedInterface = iface },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(iface)
                }
            }
        }

        TextField(
            value = macAddress,
            onValueChange = { macAddress = it },
            label = { Text("MAC Address") },
            placeholder = { Text("XX:XX:XX:XX:XX:XX") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Method selection
        Text("Execution Method:", style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("A", "B", "C").forEach { method ->
                Button(
                    onClick = { executionMethod = method },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(method)
                }
            }
        }

        TextField(
            value = ssidTarget,
            onValueChange = { ssidTarget = it },
            label = { Text("SSID Target (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Auto-apply on boot:", style = MaterialTheme.typography.bodySmall)
            Checkbox(
                checked = autoApply,
                onCheckedChange = { autoApply = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    onSave(
                        MacProfileEntity(
                            id = profile?.id ?: 0,
                            name = name,
                            interfaceName = selectedInterface,
                            macAddress = macAddress,
                            executionMethod = executionMethod,
                            autoApply = autoApply,
                            ssidTarget = ssidTarget.ifEmpty { null }
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}
