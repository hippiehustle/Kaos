package com.securescanner.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.securescanner.app.data.model.CreateSessionRequest
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.theme.Charcoal400
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import kotlinx.coroutines.launch

private val scanTypes = listOf("Full", "Quick", "Custom Folders", "Scheduled")
private val fileTypes = listOf("Image", "Video", "Document", "Audio", "Archive", "Executable")
private val autoActions = listOf("Move", "Rename", "Backup", "Quarantine")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanConfigScreen(
    scanRepository: ScanRepository,
    onScanStarted: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedScanType by remember { mutableStateOf("Full") }
    val targetFolders = remember { mutableStateListOf<String>() }
    var newFolder by remember { mutableStateOf("") }
    val selectedFileTypes = remember { mutableStateListOf("Image", "Video") }
    var confidenceThreshold by remember { mutableFloatStateOf(0.3f) }
    val selectedAutoActions = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scan Type
        SectionHeader(title = "Scan Type")
        CardSurface {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                scanTypes.forEach { type ->
                    FilterChip(
                        selected = selectedScanType == type,
                        onClick = { selectedScanType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MatteCyan600,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = Charcoal700,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Target Folders
        SectionHeader(title = "Target Folders")
        CardSurface {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newFolder,
                        onValueChange = { newFolder = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("/path/to/folder") },
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
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (newFolder.isNotBlank()) {
                            targetFolders.add(newFolder)
                            newFolder = ""
                        }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add folder", tint = MatteCyan600)
                    }
                }
                targetFolders.forEachIndexed { idx, folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            folder,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { targetFolders.removeAt(idx) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Charcoal400, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }

        // File Types
        SectionHeader(title = "File Types")
        CardSurface {
            Column {
                fileTypes.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = type in selectedFileTypes,
                            onCheckedChange = { checked ->
                                if (checked) selectedFileTypes.add(type) else selectedFileTypes.remove(type)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MatteCyan600,
                                uncheckedColor = Charcoal400
                            )
                        )
                        Text(type, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Confidence Threshold
        SectionHeader(title = "Confidence Threshold")
        CardSurface {
            Column {
                Text(
                    "${(confidenceThreshold * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MatteCyan600
                )
                Slider(
                    value = confidenceThreshold,
                    onValueChange = { confidenceThreshold = it },
                    valueRange = 0.1f..1f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = MatteCyan600,
                        activeTrackColor = MatteCyan600,
                        inactiveTrackColor = Charcoal700
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("10%", style = MaterialTheme.typography.bodySmall, color = Charcoal400)
                    Text("100%", style = MaterialTheme.typography.bodySmall, color = Charcoal400)
                }
            }
        }

        // Auto Actions
        SectionHeader(title = "Auto Actions")
        CardSurface {
            Column {
                autoActions.forEach { action ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = action in selectedAutoActions,
                            onCheckedChange = { checked ->
                                if (checked) selectedAutoActions.add(action) else selectedAutoActions.remove(action)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MatteCyan600,
                                uncheckedColor = Charcoal400
                            )
                        )
                        Text(action, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Start Scan Button
        Button(
            onClick = {
                scope.launch {
                    scanRepository.createSession(
                        CreateSessionRequest(
                            scanType = selectedScanType.lowercase().replace(" ", "_"),
                            targetFolders = targetFolders.toList(),
                            fileTypes = selectedFileTypes.map { it.lowercase() },
                            confidenceThreshold = confidenceThreshold,
                            autoActions = selectedAutoActions.map { it.lowercase() }
                        )
                    ).onSuccess { session ->
                        onScanStarted(session.id)
                    }.onFailure {
                        Toast.makeText(context, "Failed to start scan: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Start Custom Scan", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))
    }
}
