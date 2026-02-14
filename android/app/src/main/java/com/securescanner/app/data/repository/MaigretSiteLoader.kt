package com.securescanner.app.data.repository

import android.content.Context
import com.securescanner.app.data.model.MaigretSite
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaigretSiteLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private var cachedSites: List<MaigretSite>? = null

    fun loadSites(): List<MaigretSite> {
        cachedSites?.let { return it }
        val jsonString = context.assets.open("maigret_sites.json")
            .bufferedReader()
            .use { it.readText() }
        val sites = json.decodeFromString<List<MaigretSite>>(jsonString)
        cachedSites = sites
        return sites
    }

    fun getSitesByTag(tag: String): List<MaigretSite> {
        return loadSites().filter { site ->
            site.tags.any { it.equals(tag, ignoreCase = true) }
        }
    }

    fun getAllTags(): List<String> {
        return loadSites()
            .flatMap { it.tags }
            .distinct()
            .sorted()
    }

    fun searchSites(query: String): List<MaigretSite> {
        val lowerQuery = query.lowercase()
        return loadSites().filter { site ->
            site.name.lowercase().contains(lowerQuery) ||
            site.urlMain.lowercase().contains(lowerQuery) ||
            site.tags.any { it.lowercase().contains(lowerQuery) }
        }
    }

    val totalSiteCount: Int
        get() = loadSites().size
}
