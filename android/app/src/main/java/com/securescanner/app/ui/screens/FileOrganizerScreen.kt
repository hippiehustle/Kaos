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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.securescanner.app.data.model.OrganizeRequest
import com.securescanner.app.data.model.OrganizeResponse
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.theme.Charcoal400
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

private val organizeModes = listOf("Category", "Date", "File Type", "Custom")
private val flagCategories = listOf("Explicit", "Suggestive", "Adult", "Violent", "Disturbing")
private val fileTypeOptions = listOf("Image", "Video", "Document")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileOrganizerScreen(
    sessionId: Int? = null,
    scanRepository: ScanRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var destinationFolder by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("Category") }
    val selectedCategories = remember { mutableStateListOf<String>() }
    val selectedFileTypes = remember { mutableStateListOf<String>() }
    var isOrganizing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<OrganizeResponse?>(null) }

    // Presets
    fun applyPreset(mode: String) {
        selectedMode = mode
        when (mode) {
            "Date" -> {
                destinationFolder = "/storage/emulated/0/SecureScanner/organized/by-date"
            }
            "File Type" -> {
                destinationFolder = "/storage/emulated/0/SecureScanner/organized/by-type"
            }
        }
    }

    if (result != null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.height(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Organization Complete!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            result?.let { r ->
                Text("${r.moved} files moved", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text("${r.renamed} files renamed", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text("${r.copied} files copied", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { result = null },
                colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
            ) { Text("Organize More") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "File Organizer")

        // Destination Folder
        CardSurface {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CreateNewFolder, contentDescription = null, tint = MatteCyan600)
                    Spacer(Modifier.width(8.dp))
                    Text("Destination Folder", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = destinationFolder,
                    onValueChange = { destinationFolder = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("/path/to/destination") },
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
            }
        }

        // Organize Mode
        SectionHeader(title = "Organize Mode")
        CardSurface {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                organizeModes.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { applyPreset(mode) },
                        label = { Text(mode) },
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

        // Filter by Flag Categories
        SectionHeader(title = "Filter by Flag Category")
        CardSurface {
            Column {
                flagCategories.forEach { cat ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = cat in selectedCategories,
                            onCheckedChange = { if (it) selectedCategories.add(cat) else selectedCategories.remove(cat) },
                            colors = CheckboxDefaults.colors(checkedColor = MatteCyan600, uncheckedColor = Charcoal400)
                        )
                        Text(cat, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Filter by File Types
        SectionHeader(title = "Filter by File Type")
        CardSurface {
            Column {
                fileTypeOptions.forEach { ft ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = ft in selectedFileTypes,
                            onCheckedChange = { if (it) selectedFileTypes.add(ft) else selectedFileTypes.remove(ft) },
                            colors = CheckboxDefaults.colors(checkedColor = MatteCyan600, uncheckedColor = Charcoal400)
                        )
                        Text(ft, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Organize Button
        Button(
            onClick = {
                isOrganizing = true
                scope.launch {
                    val request = OrganizeRequest(
                        destinationFolder = destinationFolder,
                        mode = selectedMode.lowercase().replace(" ", "_"),
                        filterCategories = selectedCategories.map { it.lowercase() },
                        filterFileTypes = selectedFileTypes.map { it.lowercase() },
                        sessionId = sessionId
                    )
                    scanRepository.organizeCustom(request).onSuccess { response ->
                        result = response
                        isOrganizing = false
                    }.onFailure { e ->
                        isOrganizing = false
                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600),
            enabled = !isOrganizing
        ) {
            Icon(Icons.Filled.FolderSpecial, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(if (isOrganizing) "Organizing..." else "Organize Files")
        }

        Spacer(Modifier.height(16.dp))
    }
}
