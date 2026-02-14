package com.securescanner.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.model.LocalReport
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.theme.Charcoal400
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusSuccess
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val severities = listOf("Low", "Medium", "High", "Critical")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BugReportScreen(settingsDataStore: SettingsDataStore) {
    val scope = rememberCoroutineScope()
    val reportsJson by settingsDataStore.localReports.collectAsState(initial = "[]")

    var title by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var stepsToReproduce by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    if (submitted) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.height(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Bug Report Submitted", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Text("Thank you for your report!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { submitted = false; title = ""; description = ""; stepsToReproduce = "" },
                colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
            ) { Text("Submit Another") }
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
        SectionHeader(title = "Report a Bug")

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") },
            singleLine = true,
            colors = bugReportTextFieldColors()
        )

        CardSurface {
            Column {
                Text("Severity", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    severities.forEach { sev ->
                        FilterChip(
                            selected = severity == sev,
                            onClick = { severity = sev },
                            label = { Text(sev) },
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
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            label = { Text("Description") },
            colors = bugReportTextFieldColors()
        )

        OutlinedTextField(
            value = stepsToReproduce,
            onValueChange = { stepsToReproduce = it },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            label = { Text("Steps to Reproduce (optional)") },
            colors = bugReportTextFieldColors()
        )

        Button(
            onClick = {
                if (title.isNotBlank() && description.isNotBlank()) {
                    scope.launch {
                        val reports = try {
                            Json.decodeFromString<List<LocalReport>>(reportsJson).toMutableList()
                        } catch (_: Exception) { mutableListOf() }

                        reports.add(
                            LocalReport(
                                id = System.currentTimeMillis().toString(),
                                type = "bug",
                                title = title,
                                description = description,
                                severity = severity.lowercase(),
                                stepsToReproduce = stepsToReproduce.ifBlank { null },
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        settingsDataStore.setLocalReports(Json.encodeToString(reports))
                        submitted = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600),
            enabled = title.isNotBlank() && description.isNotBlank()
        ) {
            Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Submit Bug Report")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun bugReportTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MatteCyan600,
    unfocusedBorderColor = Charcoal700,
    cursorColor = MatteCyan400,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MatteCyan600,
    unfocusedLabelColor = Charcoal400,
    focusedPlaceholderColor = Charcoal400,
    unfocusedPlaceholderColor = Charcoal400
)
