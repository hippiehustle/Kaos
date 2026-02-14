package com.securescanner.app.data.api

import com.securescanner.app.data.model.CreateSessionRequest
import com.securescanner.app.data.model.OrganizeRequest
import com.securescanner.app.data.model.OrganizeResponse
import com.securescanner.app.data.model.ScanResult
import com.securescanner.app.data.model.ScanSession
import com.securescanner.app.data.model.SentiSightStatus
import com.securescanner.app.data.model.Stats
import com.securescanner.app.data.model.UpdateSessionRequest
import com.securescanner.app.data.model.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface SecureScannerApi {

    // Stats
    @GET("api/stats")
    suspend fun getStats(): Stats

    // Scan Sessions
    @POST("api/scan-sessions")
    suspend fun createScanSession(@Body request: CreateSessionRequest): ScanSession

    @GET("api/scan-sessions")
    suspend fun getScanSessions(): List<ScanSession>

    @GET("api/scan-sessions/active")
    suspend fun getActiveScanSessions(): List<ScanSession>

    @GET("api/scan-sessions/{id}")
    suspend fun getScanSession(@Path("id") id: Int): ScanSession

    @PUT("api/scan-sessions/{id}")
    suspend fun updateScanSession(
        @Path("id") id: Int,
        @Body request: UpdateSessionRequest
    ): ScanSession

    // Scan Results
    @GET("api/scan-results")
    suspend fun getScanResults(): List<ScanResult>

    @GET("api/scan-results/{sessionId}")
    suspend fun getScanResultsBySession(@Path("sessionId") sessionId: Int): List<ScanResult>

    @GET("api/nsfw-results")
    suspend fun getNsfwResults(): List<ScanResult>

    // File Upload
    @Multipart
    @POST("api/upload")
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>,
        @Part("sessionId") sessionId: RequestBody? = null
    ): UploadResponse

    // File Organization
    @POST("api/organize-files/{sessionId}")
    suspend fun organizeFilesBySession(@Path("sessionId") sessionId: Int): OrganizeResponse

    @POST("api/organize-all")
    suspend fun organizeAllFiles(): OrganizeResponse

    @POST("api/organize-custom")
    suspend fun organizeCustom(@Body request: OrganizeRequest): OrganizeResponse

    // Export
    @GET("api/export/report")
    suspend fun exportReport(): Response<okhttp3.ResponseBody>

    // Admin
    @GET("api/admin/sentisight-status")
    suspend fun getSentiSightStatus(): SentiSightStatus

    @POST("api/admin/sentisight-toggle")
    suspend fun toggleSentiSight(): SentiSightStatus

    // Data Management
    @DELETE("api/scan-history")
    suspend fun clearScanHistory(): Response<Unit>
}
