package com.securescanner.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ScanSession(
    val id: Int = 0,
    val userId: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: String = "active", // active, completed, paused, failed
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val nsfwFound: Int = 0,
    val scanType: String = "full", // full, quick, custom, scheduled
    val targetFolders: List<String> = emptyList(),
    val fileTypes: List<String> = emptyList(),
    val confidenceThreshold: Float = 0.3f,
    val autoActions: List<String> = emptyList(),
    val customSettings: String? = null
)

@Serializable
data class ScanResult(
    val id: Int = 0,
    val sessionId: Int = 0,
    val filename: String = "",
    val filepath: String = "",
    val fileType: String = "image", // image, video, document
    val isNsfw: Boolean = false,
    val confidence: Float = 0f,
    val processed: Boolean = false,
    val flagCategory: String? = null, // explicit, suggestive, adult, violent, disturbing
    val originalPath: String = "",
    val newPath: String? = null,
    val actionTaken: String = "none", // none, moved, renamed, backed_up, deleted, copied
    val isProjectFile: Boolean = false,
    val relativePath: String = "",
    val createdAt: String? = null
)

@Serializable
data class Stats(
    val totalFiles: Int = 0,
    val nsfwFound: Int = 0,
    val processed: Int = 0
)

@Serializable
data class UploadResponse(
    val session: ScanSession,
    val results: List<ScanResult>
)

@Serializable
data class OrganizeRequest(
    val destinationFolder: String = "",
    val mode: String = "category", // category, date, filetype, custom
    val filterCategories: List<String> = emptyList(),
    val filterFileTypes: List<String> = emptyList(),
    val sessionId: Int? = null
)

@Serializable
data class OrganizeResponse(
    val moved: Int = 0,
    val renamed: Int = 0,
    val copied: Int = 0
)

@Serializable
data class SentiSightStatus(
    val available: Boolean = false,
    val enabled: Boolean = false
)

@Serializable
data class CreateSessionRequest(
    val scanType: String = "full",
    val targetFolders: List<String> = emptyList(),
    val fileTypes: List<String> = emptyList(),
    val confidenceThreshold: Float = 0.3f,
    val autoActions: List<String> = emptyList(),
    val customSettings: String? = null
)

@Serializable
data class UpdateSessionRequest(
    val status: String // active, paused, completed, failed
)

// Local-only models for bug reports / feature requests
@Serializable
data class LocalReport(
    val id: String = "",
    val type: String = "", // bug, feature
    val title: String = "",
    val description: String = "",
    val severity: String? = null, // low, medium, high, critical (bug only)
    val category: String? = null, // enhancement, new_feature, ui_ux, integration, performance (feature only)
    val priority: String? = null, // nice_to_have, important, essential (feature only)
    val stepsToReproduce: String? = null, // bug only
    val timestamp: Long = 0L
)

// OSINT - Maigret site model (loaded from bundled asset)
@Serializable
data class MaigretSite(
    val name: String = "",
    val url: String = "", // URL template with {username} placeholder
    val urlMain: String = "",
    val checkType: String = "status_code", // status_code, message, response_url
    val tags: List<String> = emptyList(),
    val errorMsg: String? = null // For "message" checkType
)

// OSINT - Username check result for a single site
data class UsernameCheckResult(
    val site: MaigretSite,
    val found: Boolean,
    val url: String, // Constructed URL with actual username
    val status: CheckStatus = CheckStatus.PENDING,
    val httpStatus: Int? = null
)

enum class CheckStatus {
    PENDING, CHECKING, FOUND, NOT_FOUND, ERROR
}

// OSINT Industries API models
@Serializable
data class OsintIndustriesResponse(
    val data: List<OsintIndustriesResult> = emptyList(),
    val status: String? = null,
    val message: String? = null
)

@Serializable
data class OsintIndustriesResult(
    val module: String = "",
    val data: kotlinx.serialization.json.JsonElement? = null,
    val found: Boolean = false
)

@Serializable
data class OsintIndustriesCredits(
    val credits: Int = 0,
    val used: Int = 0,
    val remaining: Int = 0
)
