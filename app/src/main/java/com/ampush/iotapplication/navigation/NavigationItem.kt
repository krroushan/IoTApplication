package com.ampush.iotapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : NavigationItem("dashboard", "Dashboard", Icons.Default.Home)
    object Analytics : NavigationItem("analytics", "Analytics", Icons.Default.Info)
    object History : NavigationItem("history", "History", Icons.Default.List)
    object Profile : NavigationItem("profile", "Profile", Icons.Default.Person)
}

val navigationItems = listOf(
    NavigationItem.Dashboard,
    NavigationItem.Analytics,
    NavigationItem.History,
    NavigationItem.Profile
)
