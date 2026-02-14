package com.securescanner.app.data.repository

import com.securescanner.app.data.api.OsintIndustriesApi
import com.securescanner.app.data.logging.AppLogger
import com.securescanner.app.data.model.CheckStatus
import com.securescanner.app.data.model.MaigretSite
import com.securescanner.app.data.model.OsintIndustriesCredits
import com.securescanner.app.data.model.UsernameCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OsintRepository @Inject constructor(
    private val osintIndustriesApi: OsintIndustriesApi,
    private val maigretSiteLoader: MaigretSiteLoader,
    private val logger: AppLogger
) {
    // Lightweight HTTP client for Maigret checks (short timeouts)
    private val checkClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // Concurrency limiter for Maigret checks
    private val semaphore = Semaphore(20)

    fun getAllSites(): List<MaigretSite> = maigretSiteLoader.loadSites()

    /** Only sites with a valid URL template containing {username} */
    fun getSearchableSites(): List<MaigretSite> =
        maigretSiteLoader.loadSites().filter { it.url.contains("{username}") }

    fun getAllTags(): List<String> = maigretSiteLoader.getAllTags()

    fun searchSites(query: String): List<MaigretSite> = maigretSiteLoader.searchSites(query)

    val totalSiteCount: Int get() = maigretSiteLoader.totalSiteCount

    val searchableSiteCount: Int get() = getSearchableSites().size

    /**
     * Check a username across searchable Maigret sites, emitting results as they complete.
     * Only checks sites that have a valid {username} URL template.
     */
    fun checkUsername(
        username: String,
        sites: List<MaigretSite>? = null
    ): Flow<UsernameCheckResult> = flow {
        val targetSites = (sites ?: getSearchableSites()).filter { it.url.contains("{username}") }
        logger.i("OSINT", "Starting username search for '$username' across ${targetSites.size} sites")

        coroutineScope {
            val results = targetSites.map { site ->
                async {
                    semaphore.withPermit {
                        checkSingleSite(username, site)
                    }
                }
            }
            for (deferred in results) {
                emit(deferred.await())
            }
        }
        logger.i("OSINT", "Username search complete for '$username'")
    }.flowOn(Dispatchers.IO)

    /**
     * Check a batch of sites and return all results at once.
     */
    suspend fun checkUsernameBatch(
        username: String,
        sites: List<MaigretSite>
    ): List<UsernameCheckResult> = coroutineScope {
        val searchable = sites.filter { it.url.contains("{username}") }
        searchable.map { site ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    checkSingleSite(username, site)
                }
            }
        }.awaitAll()
    }

    private fun checkSingleSite(username: String, site: MaigretSite): UsernameCheckResult {
        val url = site.url.replace("{username}", username)

        // Skip invalid URLs
        if (!url.startsWith("http")) {
            return UsernameCheckResult(
                site = site, found = false, url = url,
                status = CheckStatus.ERROR, httpStatus = null
            )
        }

        return try {
            val requestBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

            // For "message" checkType, we need the body — use GET
            // For others, HEAD is faster
            if (site.checkType == "message") {
                requestBuilder.get()
            } else {
                requestBuilder.head()
            }

            val response = checkClient.newCall(requestBuilder.build()).execute()
            val statusCode = response.code

            val found = when (site.checkType) {
                "status_code" -> {
                    response.close()
                    statusCode in 200..299
                }
                "message" -> {
                    // Check if the error message is absent (= profile found)
                    val body = response.body?.string() ?: ""
                    response.close()
                    if (statusCode !in 200..299) {
                        false
                    } else if (site.errorMsg.isNullOrEmpty()) {
                        // No error message to check — fall back to status code
                        true
                    } else {
                        // Profile found if the error message is NOT present
                        !body.contains(site.errorMsg, ignoreCase = true)
                    }
                }
                "response_url" -> {
                    // If the request wasn't redirected to a generic page, the profile exists
                    val finalUrl = response.request.url.toString()
                    response.close()
                    statusCode in 200..399 && finalUrl.contains(username, ignoreCase = true)
                }
                else -> {
                    response.close()
                    statusCode in 200..299
                }
            }

            UsernameCheckResult(
                site = site, found = found, url = url,
                status = if (found) CheckStatus.FOUND else CheckStatus.NOT_FOUND,
                httpStatus = statusCode
            )
        } catch (e: Exception) {
            UsernameCheckResult(
                site = site, found = false, url = url,
                status = CheckStatus.ERROR, httpStatus = null
            )
        }
    }

    // ─── OSINT Industries ─────────────────────────────────────────────

    suspend fun osintIndustriesSearch(
        apiKey: String,
        type: String, // "email" or "phone"
        query: String
    ): Result<JsonObject> {
        logger.i("OSINT-Industries", "Searching $type: $query")
        return runCatching {
            val response = osintIndustriesApi.search(apiKey, type, query)
            if (response.isSuccessful) {
                logger.s("OSINT-Industries", "Search succeeded for $query")
                response.body() ?: throw Exception("Empty response")
            } else {
                val msg = "API error: ${response.code()} ${response.message()}"
                logger.e("OSINT-Industries", msg)
                throw Exception(msg)
            }
        }
    }

    suspend fun osintIndustriesCredits(apiKey: String): Result<OsintIndustriesCredits> {
        logger.d("OSINT-Industries", "Checking credits")
        return runCatching { osintIndustriesApi.getCredits(apiKey) }
    }
}
