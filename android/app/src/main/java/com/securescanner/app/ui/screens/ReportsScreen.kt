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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securescanner.app.data.model.ScanResult
import com.securescanner.app.data.model.Stats
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.LoadingCard
import com.securescanner.app.ui.components.SectionHeader
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.FlagExplicit
import com.securescanner.app.ui.theme.FlagSuggestive
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val stats: Stats = Stats(),
    val nsfwResults: List<ScanResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val statsResult = scanRepository.getStats()
            val nsfwResult = scanRepository.getNsfwResults()

            _uiState.value = ReportsUiState(
                stats = statsResult.getOrDefault(Stats()),
                nsfwResults = nsfwResult.getOrDefault(emptyList()),
                isLoading = false,
                error = statsResult.exceptionOrNull()?.message
            )
        }
    }

    fun exportReport(context: Context) {
        viewModelScope.launch {
            scanRepository.exportReport().onSuccess { bytes ->
                Toast.makeText(context, "Report exported (${bytes.size} bytes)", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Export failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.isLoading) {
            repeat(3) { LoadingCard(modifier = Modifier.padding(vertical = 4.dp)) }
            return@Column
        }

        // Scan Summary
        SectionHeader(title = "Scan Summary")
        CardSurface {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Assessment, contentDescription = null, tint = MatteCyan600)
                    Spacer(Modifier.width(8.dp))
                    Text("Overview", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(12.dp))

                SummaryRow("Total Files Scanned", state.stats.totalFiles.toString())
                SummaryRow("NSFW Detected", state.stats.nsfwFound.toString())
                Spacer(Modifier.height(8.dp))

                val progress = if (state.stats.totalFiles > 0) {
                    state.stats.processed.toFloat() / state.stats.totalFiles
                } else 0f
                Text(
                    "Processing Progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MatteCyan600,
                    trackColor = Charcoal700,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${(progress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MatteCyan600
                )
            }
        }

        // Risk Assessment
        SectionHeader(title = "Risk Assessment")
        CardSurface {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = MatteCyan600)
                    Spacer(Modifier.width(8.dp))
                    Text("Breakdown", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(12.dp))

                val highRisk = state.nsfwResults.count { (it.confidence) >= 0.8f }
                val mediumRisk = state.nsfwResults.count { it.confidence in 0.5f..0.8f }
                val safe = state.stats.totalFiles - state.stats.nsfwFound

                RiskRow("High Risk", highRisk, FlagExplicit)
                RiskRow("Medium Risk", mediumRisk, FlagSuggestive)
                RiskRow("Safe", safe, StatusSuccess)
            }
        }

        // Export
        SectionHeader(title = "Export")
        CardSurface {
            Column {
                Text(
                    "Download a detailed report of all scan results",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.exportReport(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Download Report (JSON)")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun RiskRow(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = color, modifier = androidx.compose.ui.Modifier.padding(end = 8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(count.toString(), style = MaterialTheme.typography.titleMedium, color = color)
    }
}
