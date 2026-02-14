package com.securescanner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securescanner.app.data.model.ScanResult
import com.securescanner.app.data.model.ScanSession
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.FlagBadge
import com.securescanner.app.ui.components.LoadingCard
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.Charcoal900
import com.securescanner.app.ui.theme.FlagExplicit
import com.securescanner.app.ui.theme.MatteCyan400
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusSuccess
import com.securescanner.app.ui.theme.StatusWarning
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanLandingUiState(
    val session: ScanSession? = null,
    val flaggedResults: List<ScanResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ScanLandingViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanLandingUiState())
    val uiState: StateFlow<ScanLandingUiState> = _uiState
    private var sessionId: Int = 0

    fun loadSession(id: Int) {
        sessionId = id
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = _uiState.value.session == null)
            val sessionResult = scanRepository.getSession(sessionId)
            val resultsResult = scanRepository.getScanResultsBySession(sessionId)

            _uiState.value = ScanLandingUiState(
                session = sessionResult.getOrNull(),
                flaggedResults = resultsResult.getOrDefault(emptyList()).filter { it.isNsfw }.take(10),
                isLoading = false,
                error = sessionResult.exceptionOrNull()?.message
            )
        }
    }

    fun pauseResume() {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val newStatus = if (session.status == "active") "paused" else "active"
            scanRepository.updateSession(sessionId, newStatus)
            refresh()
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            scanRepository.updateSession(sessionId, "completed")
            refresh()
        }
    }
}

@Composable
fun ScanLandingScreen(
    sessionId: Int,
    onNavigateToOrganize: (Int) -> Unit = {},
    viewModel: ScanLandingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    // Auto-refresh while active
    LaunchedEffect(state.session?.status) {
        if (state.session?.status == "active") {
            while (true) {
                delay(3000)
                viewModel.refresh()
            }
        }
    }

    if (state.isLoading) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            repeat(3) { LoadingCard(modifier = Modifier.padding(vertical = 8.dp)) }
        }
        return
    }

    val session = state.session
    if (session == null) {
        com.securescanner.app.ui.components.ErrorState(
            message = state.error ?: "Session not found",
            onRetry = { viewModel.refresh() }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status badge
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Scan #${session.id}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(12.dp))
                StatusBadge(session.status)
            }
        }

        // Progress
        item {
            CardSurface {
                Column {
                    val progress = if (session.totalFiles > 0) {
                        session.processedFiles.toFloat() / session.totalFiles
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = MatteCyan600,
                        trackColor = Charcoal700,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(session.processedFiles.toString(), style = MaterialTheme.typography.titleLarge, color = MatteCyan600)
                            Text("Processed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(session.totalFiles.toString(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("Total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(session.nsfwFound.toString(), style = MaterialTheme.typography.titleLarge, color = FlagExplicit)
                            Text("Flagged", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Controls
        if (session.status == "active" || session.status == "paused") {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.pauseResume() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (session.status == "active") StatusWarning else MatteCyan600
                        )
                    ) {
                        Icon(
                            if (session.status == "active") Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(if (session.status == "active") "Pause" else "Resume")
                    }
                    OutlinedButton(
                        onClick = { viewModel.stopScan() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FlagExplicit)
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Stop")
                    }
                }
            }
        }

        // Organize suggestion when complete
        if (session.status == "completed" && session.nsfwFound > 0) {
            item {
                CardSurface {
                    Column {
                        Text(
                            "Scan complete! Would you like to organize flagged files?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onNavigateToOrganize(session.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                        ) {
                            Icon(Icons.Filled.FolderSpecial, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Organize Files")
                        }
                    }
                }
            }
        }

        // Flagged files
        if (state.flaggedResults.isNotEmpty()) {
            item { SectionHeader(title = "Flagged Content") }
            items(state.flaggedResults) { result ->
                CardSurface {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(result.filename, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            Text("${(result.confidence * 100).toInt()}% confidence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        result.flagCategory?.let { FlagBadge(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "active" -> MatteCyan600 to "Active"
        "paused" -> StatusWarning to "Paused"
        "completed" -> StatusSuccess to "Completed"
        "failed" -> FlagExplicit to "Failed"
        else -> Charcoal700 to status
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
