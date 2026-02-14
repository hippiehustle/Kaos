package com.securescanner.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Files : Screen("files")
    data object Reports : Screen("reports")
    data object BugReport : Screen("bug-report")
    data object FeatureRequest : Screen("feature-request")
    data object About : Screen("about")
    data object Settings : Screen("settings")
    data object ScanConfig : Screen("scan-config")
    data object ScanLanding : Screen("scan/{sessionId}") {
        fun createRoute(sessionId: Int) = "scan/$sessionId"
    }
    data object FileOrganizer : Screen("organize/{sessionId}") {
        fun createRoute(sessionId: Int) = "organize/$sessionId"
    }
    data object FileOrganizerAll : Screen("organize")
    data object Admin : Screen("admin")
    data object OsintSearch : Screen("osint-search")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val requiresAdminVisibility: Boolean = false,
    val adminVisibilityKey: String? = null
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Files, "Files", Icons.Filled.Folder, Icons.Outlined.Folder),
    BottomNavItem(Screen.Reports, "Reports", Icons.Filled.Description, Icons.Outlined.Description),
    BottomNavItem(Screen.OsintSearch, "OSINT", Icons.Filled.PersonSearch, Icons.Outlined.PersonSearch),
    BottomNavItem(
        Screen.BugReport, "Bugs", Icons.Filled.BugReport, Icons.Outlined.BugReport,
        requiresAdminVisibility = true, adminVisibilityKey = "bugReport"
    ),
    BottomNavItem(
        Screen.FeatureRequest, "Ideas", Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb,
        requiresAdminVisibility = true, adminVisibilityKey = "featureRequest"
    ),
    BottomNavItem(Screen.About, "About", Icons.Filled.Info, Icons.Outlined.Info),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)
