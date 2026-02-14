package com.securescanner.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.theme.Charcoal400
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.Charcoal800
import com.securescanner.app.ui.theme.Charcoal900
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsDataStore: SettingsDataStore,
    private val scanRepository: ScanRepository
) : ViewModel() {

    fun saveServerUrl(url: String) {
        viewModelScope.launch { settingsDataStore.setServerUrl(url) }
    }

    fun saveOsintApiKey(key: String) {
        viewModelScope.launch { settingsDataStore.setOsintIndustriesApiKey(key) }
    }

    fun setAutoOrganize(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setAutoOrganize(enabled) }
    }

    fun setSecureBackup(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setSecureBackup(enabled) }
    }

    fun setDeepScan(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setDeepScan(enabled) }
    }

    fun setScanCompletionAlerts(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setScanCompletionAlerts(enabled) }
    }

    fun setDetectionAlerts(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setDetectionAlerts(enabled) }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            scanRepository.exportReport().onSuccess { bytes ->
                Toast.makeText(context, "Report exported (${bytes.size} bytes)", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Export failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearHistory(context: Context) {
        viewModelScope.launch {
            scanRepository.clearScanHistory().onSuccess {
                Toast.makeText(context, "Scan history cleared", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val serverUrl by viewModel.settingsDataStore.serverUrl.collectAsState(initial = "")
    val osintApiKey by viewModel.settingsDataStore.osintIndustriesApiKey.collectAsState(initial = "")
    val autoOrganize by viewModel.settingsDataStore.autoOrganize.collectAsState(initial = false)
    val secureBackup by viewModel.settingsDataStore.secureBackup.collectAsState(initial = false)
    val deepScan by viewModel.settingsDataStore.deepScan.collectAsState(initial = false)
    val scanAlerts by viewModel.settingsDataStore.scanCompletionAlerts.collectAsState(initial = true)
    val detectionAlerts by viewModel.settingsDataStore.detectionAlerts.collectAsState(initial = true)

    var editUrl by remember(serverUrl) { mutableStateOf(serverUrl) }
    var editApiKey by remember(osintApiKey) { mutableStateOf(osintApiKey) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Server Configuration
        SectionHeader(title = "Server Configuration")
        CardSurface {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Cloud, contentDescription = null, tint = MatteCyan600)
                    Spacer(Modifier.width(8.dp))
                    Text("Server URL", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editUrl,
                    onValueChange = { editUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("http://192.168.1.100:5000") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatteCyan600,
                        unfocusedBorderColor = Charcoal700,
                        cursorColor = MatteCyan400,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = Charcoal400,
                        unfocusedPlaceholderColor = Charcoal400
                    )
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.saveServerUrl(editUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Save Server URL")
                }
            }
        }

        // OSINT Industries API
        SectionHeader(title = "OSINT Industries")
        CardSurface {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MatteCyan600)
                    Spacer(Modifier.width(8.dp))
                    Text("API Key", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Required for email/phone OSINT lookups via osint.industries",
                    style = MaterialTheme.typography.bodySmall,
                    color = Charcoal400
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editApiKey,
                    onValueChange = { editApiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter API key") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatteCyan600,
                        unfocusedBorderColor = Charcoal700,
                        cursorColor = MatteCyan400,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = Charcoal400,
                        unfocusedPlaceholderColor = Charcoal400
                    )
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.saveOsintApiKey(editApiKey) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                ) {
                    Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Save API Key")
                }
            }
        }

        // Security Settings
        SectionHeader(title = "Security")
        CardSurface {
            Column {
                SettingToggle(
                    icon = Icons.Filled.Security,
                    title = "Auto-organize NSFW content",
                    checked = autoOrganize,
                    onCheckedChange = { viewModel.setAutoOrganize(it) }
                )
                SettingToggle(
                    icon = Icons.Filled.Lock,
                    title = "Secure backup before organizing",
                    checked = secureBackup,
                    onCheckedChange = { viewModel.setSecureBackup(it) }
                )
                SettingToggle(
                    icon = Icons.Filled.Storage,
                    title = "Deep scan mode",
                    checked = deepScan,
                    onCheckedChange = { viewModel.setDeepScan(it) }
                )
            }
        }

        // Notifications
        SectionHeader(title = "Notifications")
        CardSurface {
            Column {
                SettingToggle(
                    icon = Icons.Filled.Notifications,
                    title = "Scan completion alerts",
                    checked = scanAlerts,
                    onCheckedChange = { viewModel.setScanCompletionAlerts(it) }
                )
                SettingToggle(
                    icon = Icons.Filled.Notifications,
                    title = "Detection alerts",
                    checked = detectionAlerts,
                    onCheckedChange = { viewModel.setDetectionAlerts(it) }
                )
            }
        }

        // Data Management
        SectionHeader(title = "Data Management")
        CardSurface {
            Column {
                OutlinedButton(
                    onClick = { viewModel.exportData(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MatteCyan600)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Export All Data")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Clear Scan History")
                }
            }
        }

        // About
        SectionHeader(title = "About")
        CardSurface {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoRow("Version", "1.0.0")
                InfoRow("Detection Model", "InceptionV3")
                InfoRow("Accuracy", "~93%")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Scan History") },
            text = { Text("This will permanently delete all scan data. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory(context)
                    showClearDialog = false
                }) { Text("Clear", color = StatusError) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = MatteCyan600)
                }
            },
            containerColor = Charcoal800,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingToggle(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = MatteCyan600)
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MatteCyan600,
                uncheckedThumbColor = Charcoal400,
                uncheckedTrackColor = Charcoal700
            )
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MatteCyan400
        )
    }
}
