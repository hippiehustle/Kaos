package com.securescanner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.logging.AppLogger
import com.securescanner.app.data.logging.EnvironmentChecker
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.SecureScannerAppContent
import com.securescanner.app.ui.theme.SecureScannerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsDataStore: SettingsDataStore
    @Inject lateinit var scanRepository: ScanRepository
    @Inject lateinit var logger: AppLogger
    @Inject lateinit var environmentChecker: EnvironmentChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        logger.i("MainActivity", "App started")
        setContent {
            SecureScannerTheme {
                SecureScannerAppContent(
                    settingsDataStore = settingsDataStore,
                    scanRepository = scanRepository,
                    logger = logger,
                    environmentChecker = environmentChecker
                )
            }
        }
    }
}
