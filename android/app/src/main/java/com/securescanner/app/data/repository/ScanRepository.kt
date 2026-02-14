package com.securescanner.app.data.repository

import com.securescanner.app.data.api.SecureScannerApi
import com.securescanner.app.data.model.CreateSessionRequest
import com.securescanner.app.data.model.OrganizeRequest
import com.securescanner.app.data.model.OrganizeResponse
import com.securescanner.app.data.model.ScanResult
import com.securescanner.app.data.model.ScanSession
import com.securescanner.app.data.model.Stats
import com.securescanner.app.data.model.UpdateSessionRequest
import com.securescanner.app.data.model.UploadResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepository @Inject constructor(
    private val api: SecureScannerApi
) {
    suspend fun getStats(): Result<Stats> = runCatching { api.getStats() }

    suspend fun createSession(request: CreateSessionRequest): Result<ScanSession> =
        runCatching { api.createScanSession(request) }

    suspend fun getSessions(): Result<List<ScanSession>> =
        runCatching { api.getScanSessions() }

    suspend fun getActiveSessions(): Result<List<ScanSession>> =
        runCatching { api.getActiveScanSessions() }

    suspend fun getSession(id: Int): Result<ScanSession> =
        runCatching { api.getScanSession(id) }

    suspend fun updateSession(id: Int, status: String): Result<ScanSession> =
        runCatching { api.updateScanSession(id, UpdateSessionRequest(status)) }

    suspend fun getScanResults(): Result<List<ScanResult>> =
        runCatching { api.getScanResults() }

    suspend fun getScanResultsBySession(sessionId: Int): Result<List<ScanResult>> =
        runCatching { api.getScanResultsBySession(sessionId) }

    suspend fun getNsfwResults(): Result<List<ScanResult>> =
        runCatching { api.getNsfwResults() }

    suspend fun uploadFiles(files: List<File>, sessionId: Int? = null): Result<UploadResponse> =
        runCatching {
            val parts = files.map { file ->
                val requestBody = file.asRequestBody("application/octet-stream".toMediaType())
                MultipartBody.Part.createFormData("files", file.name, requestBody)
            }
            val sessionIdBody = sessionId?.toString()
                ?.toRequestBody("text/plain".toMediaType())
            api.uploadFiles(parts, sessionIdBody)
        }

    suspend fun organizeBySession(sessionId: Int): Result<OrganizeResponse> =
        runCatching { api.organizeFilesBySession(sessionId) }

    suspend fun organizeAll(): Result<OrganizeResponse> =
        runCatching { api.organizeAllFiles() }

    suspend fun organizeCustom(request: OrganizeRequest): Result<OrganizeResponse> =
        runCatching { api.organizeCustom(request) }

    suspend fun exportReport(): Result<ByteArray> = runCatching {
        val response = api.exportReport()
        response.body()?.bytes() ?: throw Exception("Empty response body")
    }

    suspend fun getSentiSightStatus() = runCatching { api.getSentiSightStatus() }

    suspend fun toggleSentiSight() = runCatching { api.toggleSentiSight() }

    suspend fun clearScanHistory(): Result<Unit> = runCatching {
        api.clearScanHistory()
        Unit
    }
}
