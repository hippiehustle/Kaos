package com.securescanner.app.ui.screens

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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.theme.Charcoal400
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusError
import com.securescanner.app.ui.theme.StatusWarning
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(
    settingsDataStore: SettingsDataStore,
    scanRepository: ScanRepository,
    onLock: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val premiumUnlocked by settingsDataStore.premiumUnlocked.collectAsState(initial = false)
    val sentiSightEnabled by settingsDataStore.sentiSightEnabled.collectAsState(initial = false)
    val bugReportVisible by settingsDataStore.bugReportVisible.collectAsState(initial = true)
    val featureRequestVisible by settingsDataStore.featureRequestVisible.collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MatteCyan600)
            Spacer(Modifier.width(8.dp))
            Text("Admin Panel", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        }

        // Premium Features
        SectionHeader(title = "Features")
        CardSurface {
            Column {
                AdminToggle(
                    icon = Icons.Filled.Star,
                    title = "Premium Features",
                    subtitle = "Unlock advanced scanning capabilities",
                    checked = premiumUnlocked,
                    onCheckedChange = {
                        scope.launch { settingsDataStore.setPremiumUnlocked(it) }
                    }
                )

                AdminToggle(
                    icon = Icons.Filled.Cloud,
                    title = "SentiSight.ai Detection",
                    subtitle = "Enable cloud-based content detection",
                    checked = sentiSightEnabled,
                    onCheckedChange = {
                        scope.launch {
                            settingsDataStore.setSentiSightEnabled(it)
                            scanRepository.toggleSentiSight()
                        }
                    }
                )
            }
        }

        // Navigation Visibility
        SectionHeader(title = "Navigation Visibility")
        CardSurface {
            Column {
                AdminToggle(
                    icon = Icons.Filled.BugReport,
                    title = "Bug Report",
                    subtitle = "Show bug report in navigation",
                    checked = bugReportVisible,
                    onCheckedChange = {
                        scope.launch { settingsDataStore.setBugReportVisible(it) }
                    }
                )
                AdminToggle(
                    icon = Icons.Filled.Lightbulb,
                    title = "Feature Request",
                    subtitle = "Show feature request in navigation",
                    checked = featureRequestVisible,
                    onCheckedChange = {
                        scope.launch { settingsDataStore.setFeatureRequestVisible(it) }
                    }
                )
            }
        }

        // Lock admin
        Button(
            onClick = {
                scope.launch {
                    settingsDataStore.setAdminUnlocked(false)
                    Toast.makeText(context, "Admin panel locked", Toast.LENGTH_SHORT).show()
                    onLock()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = StatusError)
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Lock Admin Panel")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AdminToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
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
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
