package com.ampush.iotapplication.navigation

sealed class AuthRoute(val route: String) {
    object Login : AuthRoute("login")
    object Register : AuthRoute("register") 
    object ForgotPassword : AuthRoute("forgot_password")
    object Main : AuthRoute("main")
}
