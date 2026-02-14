package com.securescanner.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.ui.screens.AboutScreen
import com.securescanner.app.ui.screens.AdminScreen
import com.securescanner.app.ui.screens.BugReportScreen
import com.securescanner.app.ui.screens.FeatureRequestScreen
import com.securescanner.app.ui.screens.FileOrganizerScreen
import com.securescanner.app.ui.screens.FilesScreen
import com.securescanner.app.ui.screens.HomeScreen
import com.securescanner.app.ui.screens.OsintSearchScreen
import com.securescanner.app.ui.screens.ReportsScreen
import com.securescanner.app.ui.screens.ScanConfigScreen
import com.securescanner.app.ui.screens.ScanLandingScreen
import com.securescanner.app.ui.screens.SettingsScreen

@Composable
fun SecureScannerNavGraph(
    navController: NavHostController,
    settingsDataStore: SettingsDataStore,
    scanRepository: ScanRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToScanConfig = { navController.navigate(Screen.ScanConfig.route) },
                onNavigateToScan = { id -> navController.navigate(Screen.ScanLanding.createRoute(id)) },
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToFiles = {
                    navController.navigate(Screen.Files.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onShowUpload = { /* handled in AppContent */ }
            )
        }
        composable(Screen.Files.route) {
            FilesScreen()
        }
        composable(Screen.Reports.route) {
            ReportsScreen()
        }
        composable(Screen.BugReport.route) {
            BugReportScreen(settingsDataStore = settingsDataStore)
        }
        composable(Screen.FeatureRequest.route) {
            FeatureRequestScreen(settingsDataStore = settingsDataStore)
        }
        composable(Screen.About.route) {
            AboutScreen(
                settingsDataStore = settingsDataStore,
                onAdminUnlocked = { navController.navigate(Screen.Admin.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.OsintSearch.route) {
            OsintSearchScreen()
        }
        composable(Screen.ScanConfig.route) {
            ScanConfigScreen(
                scanRepository = scanRepository,
                onScanStarted = { id ->
                    navController.navigate(Screen.ScanLanding.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.ScanLanding.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
            ScanLandingScreen(
                sessionId = sessionId,
                onNavigateToOrganize = { id ->
                    navController.navigate(Screen.FileOrganizer.createRoute(id))
                }
            )
        }
        composable(Screen.FileOrganizerAll.route) {
            FileOrganizerScreen(scanRepository = scanRepository)
        }
        composable(
            route = Screen.FileOrganizer.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: 0
            FileOrganizerScreen(sessionId = sessionId, scanRepository = scanRepository)
        }
        composable(Screen.Admin.route) {
            AdminScreen(
                settingsDataStore = settingsDataStore,
                scanRepository = scanRepository,
                onLock = { navController.popBackStack() }
            )
        }
    }
}
