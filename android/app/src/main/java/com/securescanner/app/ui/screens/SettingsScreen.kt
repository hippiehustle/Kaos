package com.securescanner.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.logging.AppLogger
import com.securescanner.app.data.logging.CheckResult
import com.securescanner.app.data.logging.EnvironmentChecker
import com.securescanner.app.data.logging.EnvironmentReport
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
import com.securescanner.app.ui.theme.StatusSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settingsDataStore: SettingsDataStore,
    private val scanRepository: ScanRepository,
    val logger: AppLogger,
    val environmentChecker: EnvironmentChecker
) : ViewModel() {

    private val _envReport = MutableStateFlow<EnvironmentReport?>(null)
    val envReport: StateFlow<EnvironmentReport?> = _envReport.asStateFlow()

    private val _envChecking = MutableStateFlow(false)
    val envChecking: StateFlow<Boolean> = _envChecking.asStateFlow()

    fun saveServerUrl(url: String, context: Context) {
        viewModelScope.launch {
            try {
                settingsDataStore.setServerUrl(url)
                logger.s("Settings", "Server URL saved: $url")
                Toast.makeText(context, "Server URL saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                logger.e("Settings", "Failed to save server URL: ${e.message}")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveOsintApiKey(key: String, context: Context) {
        viewModelScope.launch {
            try {
                settingsDataStore.setOsintIndustriesApiKey(key)
                val masked = if (key.length > 6) "${key.take(3)}***${key.takeLast(3)}" else "***"
                logger.s("Settings", "OSINT Industries API key saved ($masked)")
                Toast.makeText(context, "API key saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                logger.e("Settings", "Failed to save API key: ${e.message}")
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setAutoOrganize(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            settingsDataStore.setAutoOrganize(enabled)
            logger.i("Settings", "Auto-organize: ${if (enabled) "ON" else "OFF"}")
            Toast.makeText(context, "Auto-organize ${if (enabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setSecureBackup(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            settingsDataStore.setSecureBackup(enabled)
            logger.i("Settings", "Secure backup: ${if (enabled) "ON" else "OFF"}")
            Toast.makeText(context, "Secure backup ${if (enabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setDeepScan(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            settingsDataStore.setDeepScan(enabled)
            logger.i("Settings", "Deep scan: ${if (enabled) "ON" else "OFF"}")
            Toast.makeText(context, "Deep scan ${if (enabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setScanCompletionAlerts(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            settingsDataStore.setScanCompletionAlerts(enabled)
            logger.i("Settings", "Scan completion alerts: ${if (enabled) "ON" else "OFF"}")
            Toast.makeText(context, "Scan alerts ${if (enabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setDetectionAlerts(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            settingsDataStore.setDetectionAlerts(enabled)
            logger.i("Settings", "Detection alerts: ${if (enabled) "ON" else "OFF"}")
            Toast.makeText(context, "Detection alerts ${if (enabled) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            logger.i("Settings", "Exporting scan data...")
            scanRepository.exportReport().onSuccess { bytes ->
                logger.s("Settings", "Export succeeded (${bytes.size} bytes)")
                Toast.makeText(context, "Report exported (${bytes.size} bytes)", Toast.LENGTH_SHORT).show()
            }.onFailure {
                logger.e("Settings", "Export failed: ${it.message}")
                Toast.makeText(context, "Export failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearHistory(context: Context) {
        viewModelScope.launch {
            logger.i("Settings", "Clearing scan history...")
            scanRepository.clearScanHistory().onSuccess {
                logger.s("Settings", "Scan history cleared")
                Toast.makeText(context, "Scan history cleared", Toast.LENGTH_SHORT).show()
            }.onFailure {
                logger.e("Settings", "Failed to clear history: ${it.message}")
                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun runEnvironmentCheck() {
        viewModelScope.launch {
            _envChecking.value = true
            _envReport.value = environmentChecker.runFullCheck()
            _envChecking.value = false
        }
    }

    fun clearLog(context: Context) {
        logger.clearLog()
        Toast.makeText(context, "Log cleared", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val serverUrl by viewModel.settingsDataStore.serverUrl.collectAsState(initial = "")
    val osintApiKey by viewModel.settingsDataStore.osintIndustriesApiKey.collectAsState(initial = "")
    val autoOrganize by viewModel.settingsDataStore.autoOrganize.collectAsState(initial = false)
    val secureBackup by viewModel.settingsDataStore.secureBackup.collectAsState(initial = false)
    val deepScan by viewModel.settingsDataStore.deepScan.collectAsState(initial = false)
    val scanAlerts by viewModel.settingsDataStore.scanCompletionAlerts.collectAsState(initial = true)
    val detectionAlerts by viewModel.settingsDataStore.detectionAlerts.collectAsState(initial = true)
    val logContent by viewModel.logger.logContent.collectAsState()
    val envReport by viewModel.envReport.collectAsState()
    val envChecking by viewModel.envChecking.collectAsState()

    var editUrl by remember(serverUrl) { mutableStateOf(serverUrl) }
    var editApiKey by remember(osintApiKey) { mutableStateOf(osintApiKey) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showLogViewer by remember { mutableStateOf(false) }

    // Permission re-request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (perm, granted) ->
            val short = perm.substringAfterLast(".")
            if (granted) {
                viewModel.logger.s("Permissions", "$short: GRANTED")
            } else {
                viewModel.logger.w("Permissions", "$short: DENIED")
            }
        }
        Toast.makeText(context, "Permissions updated", Toast.LENGTH_SHORT).show()
        viewModel.runEnvironmentCheck()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Environment Check ───
        SectionHeader(title = "Environment Check")
        CardSurface {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.HealthAndSafety, contentDescription = null, tint = MatteCyan600)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "System Diagnostics",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Verify permissions, network, and search readiness",
                    style = MaterialTheme.typography.bodySmall,
                    color = Charcoal400
                )
                Spacer(Modifier.height(12.dp))

                // Check results
                if (envReport != null) {
                    envReport!!.checks.forEach { check ->
                        EnvironmentCheckRow(check)
                        Spacer(Modifier.height(6.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${envReport!!.passedCount}/${envReport!!.totalCount} checks passed",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (envReport!!.allPassed) StatusSuccess else StatusError,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.runEnvironmentCheck() },
                        enabled = !envChecking,
                        colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (envChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Checking...")
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Run Check")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            permissionLauncher.launch(viewModel.environmentChecker.getRequiredPermissions())
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MatteCyan600),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Re-grant")
                    }
                }
            }
        }

        // ─── Server Configuration ───
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
                    onClick = { viewModel.saveServerUrl(editUrl, context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Save Server URL")
                }
            }
        }

        // ─── OSINT Industries API ───
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
                    "Required for email/phone OSINT lookups via osint.industries. Username search works without this.",
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
                    onClick = { viewModel.saveOsintApiKey(editApiKey, context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                ) {
                    Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Save API Key")
                }
            }
        }

        // ─── Security Settings ───
        SectionHeader(title = "Security")
        CardSurface {
            Column {
                SettingToggle(
                    icon = Icons.Filled.Security,
                    title = "Auto-organize NSFW content",
                    checked = autoOrganize,
                    onCheckedChange = { viewModel.setAutoOrganize(it, context) }
                )
                SettingToggle(
                    icon = Icons.Filled.Lock,
                    title = "Secure backup before organizing",
                    checked = secureBackup,
                    onCheckedChange = { viewModel.setSecureBackup(it, context) }
                )
                SettingToggle(
                    icon = Icons.Filled.Storage,
                    title = "Deep scan mode",
                    checked = deepScan,
                    onCheckedChange = { viewModel.setDeepScan(it, context) }
                )
            }
        }

        // ─── Notifications ───
        SectionHeader(title = "Notifications")
        CardSurface {
            Column {
                SettingToggle(
                    icon = Icons.Filled.Notifications,
                    title = "Scan completion alerts",
                    checked = scanAlerts,
                    onCheckedChange = { viewModel.setScanCompletionAlerts(it, context) }
                )
                SettingToggle(
                    icon = Icons.Filled.Notifications,
                    title = "Detection alerts",
                    checked = detectionAlerts,
                    onCheckedChange = { viewModel.setDetectionAlerts(it, context) }
                )
            }
        }

        // ─── Data Management ───
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

        // ─── App Log ───
        SectionHeader(title = "Debug Log")
        CardSurface {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Terminal, contentDescription = null, tint = MatteCyan600)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Application Log",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${viewModel.logger.getLogSizeKb()} KB",
                        style = MaterialTheme.typography.labelSmall,
                        color = Charcoal400
                    )
                }
                Spacer(Modifier.height(8.dp))

                if (showLogViewer) {
                    Text(
                        text = logContent.ifEmpty { "(no log entries yet)" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        ),
                        color = Charcoal400,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showLogViewer = !showLogViewer },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MatteCyan600)
                    ) {
                        Text(if (showLogViewer) "Hide Log" else "View Log")
                    }
                    OutlinedButton(
                        onClick = { viewModel.clearLog(context) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                    ) {
                        Text("Clear Log")
                    }
                }
            }
        }

        // ─── About ───
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
private fun EnvironmentCheckRow(check: CheckResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (check.passed) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = null,
            tint = if (check.passed) StatusSuccess else StatusError,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                check.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                check.detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (check.passed) Charcoal400 else StatusError
            )
        }
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
