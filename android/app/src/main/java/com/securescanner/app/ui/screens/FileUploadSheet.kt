package com.securescanner.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.theme.Charcoal700
import com.securescanner.app.ui.theme.Charcoal800
import com.securescanner.app.ui.theme.Charcoal900
import com.securescanner.app.ui.theme.MatteCyan600
import com.securescanner.app.ui.theme.StatusError
import com.securescanner.app.ui.theme.StatusSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileUploadSheet(
    onDismiss: () -> Unit,
    scanRepository: ScanRepository,
    onScanStarted: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedFiles = remember { mutableStateListOf<Uri>() }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var uploadComplete by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedFiles.clear()
        selectedFiles.addAll(uris)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Charcoal900
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Upload Files for Scanning",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            // File picker button
            Button(
                onClick = {
                    filePickerLauncher.launch(arrayOf("image/*", "video/*"))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal700),
                enabled = !isUploading
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select Files")
            }

            // Selected files list
            if (selectedFiles.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${selectedFiles.size} file(s) selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MatteCyan600
                )
                Spacer(Modifier.height(8.dp))

                selectedFiles.forEachIndexed { index, uri ->
                    val fileName = uri.lastPathSegment ?: "Unknown file"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        if (!isUploading) {
                            IconButton(
                                onClick = { selectedFiles.removeAt(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Remove",
                                    tint = StatusError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Upload progress
            if (isUploading) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = MatteCyan600,
                    trackColor = Charcoal700,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Upload complete
            if (uploadComplete) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusSuccess)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload complete!", color = StatusSuccess)
                }
            }

            // Error
            uploadError?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(error, color = StatusError, style = MaterialTheme.typography.bodySmall)
            }

            // Upload button
            if (selectedFiles.isNotEmpty() && !isUploading && !uploadComplete) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        isUploading = true
                        uploadError = null
                        scope.launch {
                            val files = withContext(Dispatchers.IO) {
                                selectedFiles.mapNotNull { uri ->
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(uri)
                                        val fileName = uri.lastPathSegment ?: "file"
                                        val tempFile = File(context.cacheDir, fileName)
                                        inputStream?.use { input ->
                                            tempFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        tempFile
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }

                            uploadProgress = 0.5f
                            scanRepository.uploadFiles(files).onSuccess { response ->
                                uploadProgress = 1f
                                uploadComplete = true
                                isUploading = false
                                onScanStarted(response.session.id)
                            }.onFailure { e ->
                                isUploading = false
                                uploadError = "Upload failed: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
                ) {
                    Text("Upload & Scan")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
