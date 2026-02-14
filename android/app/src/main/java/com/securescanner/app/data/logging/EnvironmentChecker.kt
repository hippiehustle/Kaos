package com.securescanner.app.data.logging

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.repository.MaigretSiteLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class CheckResult(
    val name: String,
    val passed: Boolean,
    val detail: String
)

data class EnvironmentReport(
    val checks: List<CheckResult>,
    val allPassed: Boolean = checks.all { it.passed },
    val passedCount: Int = checks.count { it.passed },
    val totalCount: Int = checks.size
)

@Singleton
class EnvironmentChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore,
    private val maigretSiteLoader: MaigretSiteLoader,
    private val logger: AppLogger
) {
    private val quickClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun runFullCheck(): EnvironmentReport {
        logger.i("EnvCheck", "Starting full environment check")
        val checks = mutableListOf<CheckResult>()

        checks.add(checkPermissions())
        checks.add(checkNetworkConnectivity())
        checks.add(checkMaigretSites())
        checks.add(checkMaigretReachability())
        checks.add(checkServerUrl())
        checks.add(checkOsintApiKey())

        val report = EnvironmentReport(checks)
        logger.i("EnvCheck", "Environment check complete: ${report.passedCount}/${report.totalCount} passed")
        return report
    }

    private fun checkPermissions(): CheckResult {
        val missing = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) missing.add("READ_MEDIA_IMAGES")
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED) missing.add("READ_MEDIA_VIDEO")
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) missing.add("POST_NOTIFICATIONS")
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) missing.add("READ_EXTERNAL_STORAGE")
        }

        return if (missing.isEmpty()) {
            logger.s("EnvCheck", "All permissions granted")
            CheckResult("Permissions", true, "All required permissions granted")
        } else {
            logger.w("EnvCheck", "Missing permissions: ${missing.joinToString()}")
            CheckResult("Permissions", false, "Missing: ${missing.joinToString()}")
        }
    }

    private fun checkNetworkConnectivity(): CheckResult {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        return if (hasInternet) {
            logger.s("EnvCheck", "Network connectivity OK")
            CheckResult("Network", true, "Internet connection available")
        } else {
            logger.e("EnvCheck", "No network connectivity")
            CheckResult("Network", false, "No internet connection detected")
        }
    }

    private fun checkMaigretSites(): CheckResult {
        return try {
            val sites = maigretSiteLoader.loadSites()
            val withUrl = sites.count { it.url.contains("{username}") }
            logger.s("EnvCheck", "Maigret: ${sites.size} sites loaded, $withUrl searchable")
            CheckResult("Maigret Database", true, "${sites.size} sites loaded ($withUrl searchable)")
        } catch (e: Exception) {
            logger.e("EnvCheck", "Maigret load failed: ${e.message}")
            CheckResult("Maigret Database", false, "Failed to load: ${e.message}")
        }
    }

    private suspend fun checkMaigretReachability(): CheckResult = withContext(Dispatchers.IO) {
        try {
            // Test a well-known site from the list
            val request = Request.Builder()
                .url("https://github.com")
                .head()
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = quickClient.newCall(request).execute()
            val code = response.code
            response.close()
            if (code in 200..399) {
                logger.s("EnvCheck", "External site reachability OK (github.com: $code)")
                CheckResult("Site Reachability", true, "External sites reachable (tested github.com)")
            } else {
                logger.w("EnvCheck", "External site returned $code")
                CheckResult("Site Reachability", false, "github.com returned HTTP $code")
            }
        } catch (e: Exception) {
            logger.e("EnvCheck", "Site reachability test failed: ${e.message}")
            CheckResult("Site Reachability", false, "Cannot reach external sites: ${e.message}")
        }
    }

    private suspend fun checkServerUrl(): CheckResult {
        val url = settingsDataStore.serverUrl.first()
        if (url.isBlank()) {
            logger.i("EnvCheck", "Server URL not configured (optional)")
            return CheckResult("Server URL", true, "Not configured (optional for OSINT)")
        }
        return withContext(Dispatchers.IO) {
            try {
                val testUrl = if (url.endsWith("/")) "${url}api/stats" else "$url/api/stats"
                val request = Request.Builder().url(testUrl).head().build()
                val response = quickClient.newCall(request).execute()
                val code = response.code
                response.close()
                if (code in 200..399) {
                    logger.s("EnvCheck", "Server reachable at $url (HTTP $code)")
                    CheckResult("Server URL", true, "Reachable ($url)")
                } else {
                    logger.w("EnvCheck", "Server returned HTTP $code at $url")
                    CheckResult("Server URL", false, "HTTP $code from $url")
                }
            } catch (e: Exception) {
                logger.e("EnvCheck", "Server unreachable at $url: ${e.message}")
                CheckResult("Server URL", false, "Unreachable: ${e.message}")
            }
        }
    }

    private suspend fun checkOsintApiKey(): CheckResult {
        val key = settingsDataStore.osintIndustriesApiKey.first()
        return if (key.isBlank()) {
            logger.i("EnvCheck", "OSINT Industries API key not set (optional)")
            CheckResult("OSINT Industries API", true, "Not configured (optional — username search works without it)")
        } else {
            logger.s("EnvCheck", "OSINT Industries API key is set")
            CheckResult("OSINT Industries API", true, "API key configured")
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
