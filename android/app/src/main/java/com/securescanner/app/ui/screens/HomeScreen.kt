package com.securescanner.app.ui.screens

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securescanner.app.data.model.ScanResult
import com.securescanner.app.data.model.ScanSession
import com.securescanner.app.data.model.Stats
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.FlagBadge
import com.securescanner.app.ui.components.LoadingCard
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.components.StatItem
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.Charcoal900
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusError
import com.securescanner.app.ui.theme.StatusSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val stats: Stats = Stats(),
    val activeSession: ScanSession? = null,
    val recentFindings: List<ScanResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val statsResult = scanRepository.getStats()
            val activeResult = scanRepository.getActiveSessions()
            val nsfwResult = scanRepository.getNsfwResults()

            _uiState.value = _uiState.value.copy(
                stats = statsResult.getOrDefault(Stats()),
                activeSession = activeResult.getOrNull()?.firstOrNull(),
                recentFindings = nsfwResult.getOrDefault(emptyList()).takeLast(3).reversed(),
                isLoading = false,
                error = if (statsResult.isFailure && activeResult.isFailure) "Unable to connect to server" else null
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToScanConfig: () -> Unit = {},
    onNavigateToScan: (Int) -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToFiles: () -> Unit = {},
    onShowUpload: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats
        if (state.isLoading) {
            LoadingCard()
        } else {
            CardSurface {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Total Files",
                        value = state.stats.totalFiles.toString(),
                        color = MatteCyan600
                    )
                    StatItem(
                        label = "NSFW Found",
                        value = state.stats.nsfwFound.toString(),
                        color = StatusError
                    )
                    StatItem(
                        label = "Processed",
                        value = if (state.stats.totalFiles > 0) {
                            "${(state.stats.processed * 100 / state.stats.totalFiles)}%"
                        } else "0%",
                        color = StatusSuccess
                    )
                }
            }
        }

        // Scanning Status
        state.activeSession?.let { session ->
            CardSurface {
                Column {
                    SectionHeader(title = "Scanning in Progress")
                    Spacer(Modifier.height(8.dp))
                    val progress = if (session.totalFiles > 0) {
                        session.processedFiles.toFloat() / session.totalFiles
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MatteCyan600,
                        trackColor = Charcoal700,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${session.processedFiles} / ${session.totalFiles} files processed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onNavigateToScan(session.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Scan Progress")
                    }
                }
            }
        }

        // Error state
        state.error?.let { error ->
            CardSurface {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = StatusError)
                    Spacer(Modifier.width(8.dp))
                    Text(error, style = MaterialTheme.typography.bodyMedium, color = StatusError)
                }
            }
        }

        // Quick Actions
        SectionHeader(title = "Quick Actions")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Filled.PlayArrow,
                label = "Start Scan",
                onClick = onNavigateToScanConfig,
                modifier = Modifier.weight(1f),
                isPrimary = true
            )
            ActionButton(
                icon = Icons.Filled.Tune,
                label = "Custom Scan",
                onClick = onNavigateToScanConfig,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Filled.CloudUpload,
                label = "Upload Files",
                onClick = onShowUpload,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Filled.Bolt,
                label = "Quick Scan",
                onClick = onNavigateToScanConfig,
                modifier = Modifier.weight(1f)
            )
        }
        ActionButton(
            icon = Icons.Filled.Assessment,
            label = "View Reports",
            onClick = onNavigateToReports,
            modifier = Modifier.fillMaxWidth()
        )

        // Recent Findings
        if (state.recentFindings.isNotEmpty()) {
            SectionHeader(title = "Recent Findings")
            state.recentFindings.forEach { result ->
                CardSurface {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result.filename,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = result.filepath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        result.flagCategory?.let { FlagBadge(it) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Charcoal700,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
