package com.ampush.iotapplication.ui.screens.auth

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SimpleAuthFlow(
    onAuthComplete: (String) -> Unit
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            SimpleLoginScreen(
                onLoginSuccess = { email -> onAuthComplete(email) }
            )
        }
    }
}
