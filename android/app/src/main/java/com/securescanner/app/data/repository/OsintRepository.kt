package com.securescanner.app.data.repository

import com.securescanner.app.data.api.OsintIndustriesApi
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
    private val maigretSiteLoader: MaigretSiteLoader
) {
    // Lightweight HTTP client for Maigret checks (short timeouts)
    private val checkClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // Concurrency limiter for Maigret checks
    private val semaphore = Semaphore(20)

    fun getAllSites(): List<MaigretSite> = maigretSiteLoader.loadSites()

    fun getAllTags(): List<String> = maigretSiteLoader.getAllTags()

    fun searchSites(query: String): List<MaigretSite> = maigretSiteLoader.searchSites(query)

    val totalSiteCount: Int get() = maigretSiteLoader.totalSiteCount

    /**
     * Check a username across all Maigret sites, emitting results as they complete.
     * Uses a semaphore to limit concurrent requests.
     */
    fun checkUsername(
        username: String,
        sites: List<MaigretSite> = getAllSites()
    ): Flow<UsernameCheckResult> = flow {
        coroutineScope {
            val results = sites.map { site ->
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
    }.flowOn(Dispatchers.IO)

    /**
     * Check a batch of sites and return all results at once.
     */
    suspend fun checkUsernameBatch(
        username: String,
        sites: List<MaigretSite>
    ): List<UsernameCheckResult> = coroutineScope {
        sites.map { site ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    checkSingleSite(username, site)
                }
            }
        }.awaitAll()
    }

    private fun checkSingleSite(username: String, site: MaigretSite): UsernameCheckResult {
        val url = site.url.replace("{username}", username)
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = checkClient.newCall(request).execute()
            val statusCode = response.code
            response.close()

            val found = when (site.checkType) {
                "status_code" -> statusCode in 200..299
                "response_url" -> statusCode in 200..399
                "message" -> statusCode in 200..299 // Simplified: HEAD can't check body
                else -> statusCode in 200..299
            }

            UsernameCheckResult(
                site = site,
                found = found,
                url = url,
                status = if (found) CheckStatus.FOUND else CheckStatus.NOT_FOUND,
                httpStatus = statusCode
            )
        } catch (e: Exception) {
            UsernameCheckResult(
                site = site,
                found = false,
                url = url,
                status = CheckStatus.ERROR,
                httpStatus = null
            )
        }
    }

    // ─── OSINT Industries ─────────────────────────────────────────────

    suspend fun osintIndustriesSearch(
        apiKey: String,
        type: String, // "email" or "phone"
        query: String
    ): Result<JsonObject> = runCatching {
        val response = osintIndustriesApi.search(apiKey, type, query)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response")
        } else {
            throw Exception("API error: ${response.code()} ${response.message()}")
        }
    }

    suspend fun osintIndustriesCredits(apiKey: String): Result<OsintIndustriesCredits> =
        runCatching { osintIndustriesApi.getCredits(apiKey) }
}
