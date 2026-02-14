package com.securescanner.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(
    settingsDataStore: SettingsDataStore,
    onAdminUnlocked: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo - tappable for easter egg
        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = "SecureScanner Logo",
            modifier = Modifier
                .size(80.dp)
                .clickable {
                    val now = System.currentTimeMillis()
                    if (now - lastTapTime > 3000) {
                        tapCount = 1
                    } else {
                        tapCount++
                    }
                    lastTapTime = now

                    if (tapCount >= 7) {
                        tapCount = 0
                        scope.launch {
                            settingsDataStore.setAdminUnlocked(true)
                        }
                        Toast
                            .makeText(context, "Admin panel unlocked!", Toast.LENGTH_SHORT)
                            .show()
                        onAdminUnlocked()
                    } else if (tapCount >= 4) {
                        Toast
                            .makeText(context, "${7 - tapCount} more taps...", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
            tint = MatteCyan600
        )

        Text(
            "SecureScanner",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "by Kaos Forge",
            style = MaterialTheme.typography.bodyMedium,
            color = MatteCyan400
        )

        Spacer(Modifier.height(8.dp))

        // What We Do
        AboutCard(
            icon = Icons.Filled.Info,
            title = "What We Do",
            description = "SecureScanner helps you detect and manage NSFW content in your files using advanced AI-powered analysis. Upload files, scan directories, and organize flagged content — all from your device."
        )

        // Technology
        AboutCard(
            icon = Icons.Filled.Memory,
            title = "Technology",
            description = "Detection Model: InceptionV3 (NSFWJS)\nAccuracy: ~93%\nPlatform: Native Android\nVersion: 1.0.0"
        )

        // Privacy
        AboutCard(
            icon = Icons.Filled.Lock,
            title = "Privacy",
            description = "All scanning is performed on your configured server. Files are processed securely and results are stored in your server's database. No data is shared with third parties."
        )

        // Credits
        AboutCard(
            icon = Icons.Filled.Code,
            title = "Credits",
            description = "Built with Kotlin, Jetpack Compose, and Material 3.\n\nPowered by TensorFlow.js and NSFWJS for content detection.\n\nDeveloped by Kaos Forge."
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AboutCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    CardSurface {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MatteCyan600, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
