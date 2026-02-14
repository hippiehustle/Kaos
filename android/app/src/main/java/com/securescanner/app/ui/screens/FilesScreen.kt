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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.securescanner.app.data.model.ScanResult
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.components.CardSurface
import com.securescanner.app.ui.components.ConfidenceBadge
import com.securescanner.app.ui.components.EmptyState
import com.securescanner.app.ui.components.ErrorState
import com.securescanner.app.ui.components.FlagBadge
import com.securescanner.app.ui.components.LoadingCard
import com.securescanner.app.ui.components.SafeBadge
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.MatteCyan600
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilesUiState(
    val allResults: List<ScanResult> = emptyList(),
    val nsfwResults: List<ScanResult> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val allResult = scanRepository.getScanResults()
            val nsfwResult = scanRepository.getNsfwResults()

            _uiState.value = FilesUiState(
                allResults = allResult.getOrDefault(emptyList()),
                nsfwResults = nsfwResult.getOrDefault(emptyList()),
                isLoading = false,
                error = if (allResult.isFailure) allResult.exceptionOrNull()?.message else null
            )
        }
    }
}

enum class FileFilter(val label: String) { All("All"), Flagged("Flagged"), Safe("Safe") }

@Composable
fun FilesScreen(viewModel: FilesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(FileFilter.All) }

    val filteredFiles = when (selectedFilter) {
        FileFilter.All -> state.allResults
        FileFilter.Flagged -> state.nsfwResults
        FileFilter.Safe -> state.allResults.filter { !it.isNsfw }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Filter tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FileFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MatteCyan600,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Charcoal700,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> {
                repeat(3) {
                    LoadingCard(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
            state.error != null -> {
                ErrorState(message = state.error!!, onRetry = { viewModel.refresh() })
            }
            filteredFiles.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.FolderOpen,
                    title = "No files found",
                    subtitle = "Upload files to start scanning"
                )
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredFiles) { result ->
                        FileListItem(result)
                    }
                }
            }
        }
    }
}

@Composable
private fun FileListItem(result: ScanResult) {
    CardSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getFileTypeIcon(result.fileType),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MatteCyan600
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.filename,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = if (result.actionTaken != "none") "Organized" else result.filepath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            if (result.isNsfw) {
                Column(horizontalAlignment = Alignment.End) {
                    result.flagCategory?.let { FlagBadge(it) }
                    Spacer(Modifier.height(4.dp))
                    ConfidenceBadge(result.confidence)
                }
            } else {
                SafeBadge()
            }
        }
    }
}

private fun getFileTypeIcon(fileType: String): ImageVector = when (fileType.lowercase()) {
    "image" -> Icons.Filled.Image
    "video" -> Icons.Filled.VideoFile
    "document" -> Icons.Filled.Description
    else -> Icons.Filled.Description
}
