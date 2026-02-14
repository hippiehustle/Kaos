package com.securescanner.app.data.logging

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val LOG_FILE_NAME = "securescanner.log"
        private const val MAX_FILE_SIZE = 1_048_576L // 1MB
        private const val TRUNCATE_TO = 524_288L // 512KB — keep newest half
    }

    private val logFile: File = File(context.filesDir, LOG_FILE_NAME)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    // Observable log content for live UI
    private val _logContent = MutableStateFlow("")
    val logContent: StateFlow<String> = _logContent.asStateFlow()

    init {
        if (!logFile.exists()) logFile.createNewFile()
        refreshLogContent()
        i("Logger", "App logger initialized")
    }

    fun i(tag: String, message: String) = write("INFO", tag, message)
    fun w(tag: String, message: String) = write("WARN", tag, message)
    fun e(tag: String, message: String) = write("ERROR", tag, message)
    fun d(tag: String, message: String) = write("DEBUG", tag, message)
    fun s(tag: String, message: String) = write("SUCCESS", tag, message)

    @Synchronized
    private fun write(level: String, tag: String, message: String) {
        try {
            val timestamp = dateFormat.format(Date())
            val line = "[$timestamp] $level/$tag: $message\n"
            logFile.appendText(line)

            // Rotate if too large
            if (logFile.length() > MAX_FILE_SIZE) {
                rotate()
            }

            refreshLogContent()
        } catch (ex: Exception) {
            // Fallback — don't crash the app for logging failures
            android.util.Log.e("AppLogger", "Failed to write log: ${ex.message}")
        }
    }

    private fun rotate() {
        try {
            val content = logFile.readText()
            val truncated = content.takeLast(TRUNCATE_TO.toInt())
            val marker = "--- LOG ROTATED (exceeded ${MAX_FILE_SIZE / 1024}KB) ---\n"
            logFile.writeText(marker + truncated)
            i("Logger", "Log file rotated — oldest entries removed")
        } catch (ex: Exception) {
            android.util.Log.e("AppLogger", "Failed to rotate: ${ex.message}")
        }
    }

    private fun refreshLogContent() {
        _logContent.value = try {
            if (logFile.exists()) logFile.readText() else ""
        } catch (e: Exception) {
            "(error reading log: ${e.message})"
        }
    }

    fun getLogText(): String {
        return try {
            if (logFile.exists()) logFile.readText() else "(empty)"
        } catch (e: Exception) {
            "(error reading log: ${e.message})"
        }
    }

    fun getLogSizeKb(): Long {
        return if (logFile.exists()) logFile.length() / 1024 else 0
    }

    fun clearLog() {
        logFile.writeText("")
        refreshLogContent()
        i("Logger", "Log cleared by user")
    }
}
