package com.securescanner.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.securescanner.app.data.datastore.SettingsDataStore
import com.securescanner.app.data.logging.AppLogger
import com.securescanner.app.data.logging.EnvironmentChecker
import com.securescanner.app.data.repository.ScanRepository
import com.securescanner.app.navigation.Screen
import com.securescanner.app.navigation.SecureScannerNavGraph
import com.securescanner.app.navigation.bottomNavItems
import com.securescanner.app.ui.components.BottomNavBar
import com.securescanner.app.ui.components.TopHeader
import com.securescanner.app.ui.screens.FileUploadSheet
import com.securescanner.app.ui.theme.Charcoal400
import com.securescanner.app.ui.theme.Charcoal900
import com.securescanner.app.ui.theme.MatteCyan600
import kotlinx.coroutines.launch

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
    scanRepository: ScanRepository,
    logger: AppLogger,
    environmentChecker: EnvironmentChecker
) {
    val firstBootComplete by settingsDataStore.firstBootComplete.collectAsState(initial = true)
    // Default to true so we don't flash the permission screen while DataStore loads
    var dataStoreLoaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Track when DataStore actually emits
    LaunchedEffect(firstBootComplete) {
        dataStoreLoaded = true
    }

    if (dataStoreLoaded && !firstBootComplete) {
        FirstBootPermissionScreen(
            logger = logger,
            environmentChecker = environmentChecker,
            onComplete = {
                scope.launch {
                    settingsDataStore.setFirstBootComplete(true)
                    logger.s("FirstBoot", "First boot setup complete — all permissions requested")
                }
            }
        )
    } else {
        MainAppContent(
            settingsDataStore = settingsDataStore,
            scanRepository = scanRepository,
            logger = logger,
            environmentChecker = environmentChecker
        )
    }
}

@Composable
private fun FirstBootPermissionScreen(
    logger: AppLogger,
    environmentChecker: EnvironmentChecker,
    onComplete: () -> Unit
) {
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.count { it }
        val denied = results.values.count { !it }
        logger.i("Permissions", "Permission results: $granted granted, $denied denied")
        results.forEach { (perm, isGranted) ->
            val shortName = perm.substringAfterLast(".")
            if (isGranted) {
                logger.s("Permissions", "$shortName: GRANTED")
            } else {
                logger.w("Permissions", "$shortName: DENIED")
            }
        }
        permissionsGranted = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Security,
            contentDescription = null,
            tint = MatteCyan600,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "SecureScanner Setup",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "SecureScanner needs permissions to scan files and send notifications. " +
            "Grant access to enable full functionality.",
            style = MaterialTheme.typography.bodyMedium,
            color = Charcoal400,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        if (!permissionsGranted) {
            Button(
                onClick = {
                    logger.i("Permissions", "Requesting all permissions")
                    permissionLauncher.launch(environmentChecker.getRequiredPermissions())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MatteCyan600)
            ) {
                Text("Grant Permissions")
            }
        } else {
            Text(
                "Permissions processed. You can adjust them later in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = Charcoal400,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (permissionsGranted) MatteCyan600 else Charcoal400
            )
        ) {
            Text(if (permissionsGranted) "Continue" else "Skip for Now")
        }
    }
}

@Composable
private fun MainAppContent(
    settingsDataStore: SettingsDataStore,
    scanRepository: ScanRepository,
    logger: AppLogger,
    environmentChecker: EnvironmentChecker
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
