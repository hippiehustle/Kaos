package com.securescanner.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    companion object {
        // Server
        val SERVER_URL = stringPreferencesKey("server_url")

        // Security settings
        val AUTO_ORGANIZE = booleanPreferencesKey("auto_organize")
        val SECURE_BACKUP = booleanPreferencesKey("secure_backup")
        val DEEP_SCAN = booleanPreferencesKey("deep_scan")

        // Notifications
        val SCAN_COMPLETION_ALERTS = booleanPreferencesKey("scan_completion_alerts")
        val DETECTION_ALERTS = booleanPreferencesKey("detection_alerts")

        // Admin
        val ADMIN_UNLOCKED = booleanPreferencesKey("admin_unlocked")
        val PREMIUM_UNLOCKED = booleanPreferencesKey("premium_unlocked")
        val SENTISIGHT_ENABLED = booleanPreferencesKey("sentisight_enabled")
        val BUG_REPORT_VISIBLE = booleanPreferencesKey("bug_report_visible")
        val FEATURE_REQUEST_VISIBLE = booleanPreferencesKey("feature_request_visible")

        // OSINT
        val OSINT_INDUSTRIES_API_KEY = stringPreferencesKey("osint_industries_api_key")

        // App state
        val FIRST_BOOT_COMPLETE = booleanPreferencesKey("first_boot_complete")

        // Reports (stored as JSON string)
        val LOCAL_REPORTS = stringPreferencesKey("local_reports")
    }

    // Server URL
    val serverUrl: Flow<String> = dataStore.data.map { prefs ->
        prefs[SERVER_URL] ?: ""
    }

    suspend fun setServerUrl(url: String) {
        dataStore.edit { prefs -> prefs[SERVER_URL] = url }
    }

    // Security settings
    val autoOrganize: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AUTO_ORGANIZE] ?: false
    }

    suspend fun setAutoOrganize(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[AUTO_ORGANIZE] = enabled }
    }

    val secureBackup: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SECURE_BACKUP] ?: false
    }

    suspend fun setSecureBackup(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SECURE_BACKUP] = enabled }
    }

    val deepScan: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DEEP_SCAN] ?: false
    }

    suspend fun setDeepScan(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DEEP_SCAN] = enabled }
    }

    // Notifications
    val scanCompletionAlerts: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SCAN_COMPLETION_ALERTS] ?: true
    }

    suspend fun setScanCompletionAlerts(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SCAN_COMPLETION_ALERTS] = enabled }
    }

    val detectionAlerts: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DETECTION_ALERTS] ?: true
    }

    suspend fun setDetectionAlerts(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DETECTION_ALERTS] = enabled }
    }

    // Admin
    val adminUnlocked: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ADMIN_UNLOCKED] ?: false
    }

    suspend fun setAdminUnlocked(unlocked: Boolean) {
        dataStore.edit { prefs -> prefs[ADMIN_UNLOCKED] = unlocked }
    }

    val premiumUnlocked: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PREMIUM_UNLOCKED] ?: false
    }

    suspend fun setPremiumUnlocked(unlocked: Boolean) {
        dataStore.edit { prefs -> prefs[PREMIUM_UNLOCKED] = unlocked }
    }

    val sentiSightEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SENTISIGHT_ENABLED] ?: false
    }

    suspend fun setSentiSightEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SENTISIGHT_ENABLED] = enabled }
    }

    val bugReportVisible: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[BUG_REPORT_VISIBLE] ?: true
    }

    suspend fun setBugReportVisible(visible: Boolean) {
        dataStore.edit { prefs -> prefs[BUG_REPORT_VISIBLE] = visible }
    }

    val featureRequestVisible: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[FEATURE_REQUEST_VISIBLE] ?: true
    }

    suspend fun setFeatureRequestVisible(visible: Boolean) {
        dataStore.edit { prefs -> prefs[FEATURE_REQUEST_VISIBLE] = visible }
    }

    // OSINT Industries API Key
    val osintIndustriesApiKey: Flow<String> = dataStore.data.map { prefs ->
        prefs[OSINT_INDUSTRIES_API_KEY] ?: ""
    }

    suspend fun setOsintIndustriesApiKey(key: String) {
        dataStore.edit { prefs -> prefs[OSINT_INDUSTRIES_API_KEY] = key }
    }

    // First boot
    val firstBootComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[FIRST_BOOT_COMPLETE] ?: false
    }

    suspend fun setFirstBootComplete(complete: Boolean) {
        dataStore.edit { prefs -> prefs[FIRST_BOOT_COMPLETE] = complete }
    }

    // Local reports (JSON)
    val localReports: Flow<String> = dataStore.data.map { prefs ->
        prefs[LOCAL_REPORTS] ?: "[]"
    }

    suspend fun setLocalReports(json: String) {
        dataStore.edit { prefs -> prefs[LOCAL_REPORTS] = json }
    }
}
