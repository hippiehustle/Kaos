package com.securescanner.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.navigation.Screen
import com.securescanner.app.navigation.SecureScannerNavGraph
import com.securescanner.app.navigation.bottomNavItems
import com.securescanner.app.ui.components.BottomNavBar
import com.securescanner.app.ui.components.TopHeader
import com.securescanner.app.ui.screens.FileUploadSheet
import com.securescanner.app.ui.theme.Charcoal900

private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Files.route,
    Screen.Reports.route,
    Screen.OsintSearch.route,
    Screen.BugReport.route,
    Screen.FeatureRequest.route,
    Screen.About.route,
    Screen.Settings.route,
)

@Composable
fun SecureScannerAppContent(
    settingsDataStore: SettingsDataStore,
    scanRepository: ScanRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in bottomNavRoutes

    val bugVisible by settingsDataStore.bugReportVisible.collectAsState(initial = true)
    val featureVisible by settingsDataStore.featureRequestVisible.collectAsState(initial = true)

    val visibleNavItems = bottomNavItems.filter { item ->
        when (item.adminVisibilityKey) {
            "bugReport" -> bugVisible
            "featureRequest" -> featureVisible
            else -> true
        }
    }

    var showUploadSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Charcoal900,
        topBar = {
            if (showBottomNav) {
                TopHeader()
            }
        },
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    navController = navController,
                    visibleItems = visibleNavItems
                )
            }
        }
    ) { innerPadding ->
        SecureScannerNavGraph(
            navController = navController,
            settingsDataStore = settingsDataStore,
            scanRepository = scanRepository,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showUploadSheet) {
        FileUploadSheet(
            onDismiss = { showUploadSheet = false },
            scanRepository = scanRepository,
            onScanStarted = { id ->
                showUploadSheet = false
                navController.navigate(Screen.ScanLanding.createRoute(id))
            }
        )
    }
}
