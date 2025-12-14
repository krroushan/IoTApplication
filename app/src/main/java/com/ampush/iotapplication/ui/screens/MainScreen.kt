package com.ampush.iotapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ampush.iotapplication.navigation.NavigationItem
import com.ampush.iotapplication.navigation.navigationItems
import com.ampush.iotapplication.ui.components.Dashboard
import com.ampush.iotapplication.ui.components.DefaultDeviceSelectionDialog
import com.ampush.iotapplication.data.manager.DeviceManager
import com.ampush.iotapplication.data.manager.DefaultDeviceManager
import com.ampush.iotapplication.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    
    // Default device dialog state
    var showDefaultDeviceDialog by remember { mutableStateOf(false) }
    var defaultDeviceRefreshTrigger by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            val isDetailScreen = currentRoute in listOf("help_support", "privacy_policy", "terms_of_service", "fcm_debug", "delete_account")
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            NavigationItem.Dashboard.route -> "Ampush"
                            NavigationItem.Reports.route -> "Reports"
                            NavigationItem.History.route -> "Motor History"
                            NavigationItem.Profile.route -> "Profile"
                            "help_support" -> "Help & Support"
                            "privacy_policy" -> "Privacy Policy"
                            "terms_of_service" -> "Terms of Service"
                            "fcm_debug" -> "FCM Debug"
                            "delete_account" -> "Delete Account"
                            else -> "IoT Control"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (isDetailScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (!isDetailScreen) {
                        IconButton(onClick = { /* TODO: Notifications */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                        IconButton(onClick = { showDefaultDeviceDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    } else if (currentRoute == "privacy_policy") {
                        // Show "View Online" button for Privacy Policy
                        IconButton(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://ampushworks.com/privacy-policy")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Online"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Only show bottom navigation bar for main tab screens
            val showBottomBar = currentRoute in navigationItems.map { it.route }
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    navigationItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavigationItem.Dashboard.route) {
                Dashboard(refreshKey = defaultDeviceRefreshTrigger)
            }
            composable(NavigationItem.Reports.route) {
                ReportsScreen()
            }
            composable(NavigationItem.History.route) {
                HistoryScreen()
            }
            composable(NavigationItem.Profile.route) {
                ProfileScreen(navController = navController)
            }
            composable("fcm_debug") {
                FcmDebugScreen()
            }
            composable("help_support") {
                HelpSupportScreen(navController = navController)
            }
            composable("privacy_policy") {
                PrivacyPolicyScreen(navController = navController)
            }
            composable("terms_of_service") {
                TermsOfServiceScreen(navController = navController)
            }
            composable("delete_account") {
                DeleteAccountScreen(navController = navController)
            }
        }
        
        // Default Device Selection Dialog
        if (showDefaultDeviceDialog) {
            val context = LocalContext.current
            val deviceManager = remember { DeviceManager(context) }
            val defaultDeviceManager = remember { DefaultDeviceManager(context) }
            val savedDevices = remember { deviceManager.getSavedDevices() }
            val currentDefaultDevice = remember { defaultDeviceManager.getDefaultDevice() }
            
            DefaultDeviceSelectionDialog(
                devices = savedDevices,
                currentDefaultDevice = currentDefaultDevice,
                onDeviceSelected = { device ->
                    // Trigger refresh of dashboard
                    defaultDeviceRefreshTrigger++
                    showDefaultDeviceDialog = false
                },
                onDismiss = {
                    showDefaultDeviceDialog = false
                }
            )
        }
    }
}
