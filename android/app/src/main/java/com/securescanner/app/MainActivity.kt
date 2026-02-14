package com.securescanner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.SecureScannerAppContent
import com.securescanner.app.ui.theme.SecureScannerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsDataStore: SettingsDataStore
    @Inject lateinit var scanRepository: ScanRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecureScannerTheme {
                SecureScannerAppContent(
                    settingsDataStore = settingsDataStore,
                    scanRepository = scanRepository
                )
            }
        }
    }
}
