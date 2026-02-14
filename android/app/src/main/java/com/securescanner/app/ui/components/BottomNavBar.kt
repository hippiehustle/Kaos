package com.securescanner.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.securescanner.app.navigation.BottomNavItem
import com.securescanner.app.navigation.bottomNavItems
import com.securescanner.app.ui.theme.Charcoal900
import com.securescanner.app.ui.theme.Charcoal400
import com.securescanner.app.ui.theme.MatteCyan600

@Composable
fun BottomNavBar(
    navController: NavController,
    visibleItems: List<BottomNavItem> = bottomNavItems
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Charcoal900,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        visibleItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MatteCyan600,
                    selectedTextColor = MatteCyan600,
                    unselectedIconColor = Charcoal400,
                    unselectedTextColor = Charcoal400,
                    indicatorColor = MatteCyan600.copy(alpha = 0.12f)
                )
            )
        }
    }
}
