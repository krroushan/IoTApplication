package com.ampush.iotapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ampush.iotapplication.ui.theme.IoTApplicationTheme
import com.ampush.iotapplication.ui.screens.SplashScreen
import com.ampush.iotapplication.ui.screens.MainScreen
import com.ampush.iotapplication.ui.screens.auth.SimpleAuthFlow
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.PermissionManager
import com.ampush.iotapplication.utils.SessionManager
import com.ampush.iotapplication.repository.CustomerRepository
import com.ampush.iotapplication.data.manager.DeviceManager
import com.ampush.iotapplication.data.manager.FcmTokenManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    
    // Coroutine scope for API calls
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val customerRepository = CustomerRepository()
    private lateinit var deviceManager: DeviceManager
    private lateinit var fcmTokenManager: FcmTokenManager
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, granted) ->
            Logger.d("Permission $permission granted: $granted", "PERMISSIONS")
        }
        PermissionManager.logPermissionStatus(this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)
        
        // Initialize managers after context is available
        deviceManager = DeviceManager(this)
        fcmTokenManager = FcmTokenManager(this)
        
        // Initialize FCM token
        fcmTokenManager.initializeFcmToken()
        
        // Test logging - should appear immediately in Logcat
        Log.d("IoTMotorControl", "🚀 DIRECT LOG TEST - APP STARTED")
        Logger.d("🚀 APP STARTED - LOGGING TEST", "TEST")
        Logger.i("IoT Motor Control App Launched", "APP_STARTUP")
        Logger.logUIEvent("MainActivity", "onCreate", "App started")
        
        // Check and request SMS permissions
        checkAndRequestPermissions()
        
        enableEdgeToEdge()
        setContent {
            IoTApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }
                val sessionManager = remember { SessionManager(this@MainActivity) }
                var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
                
                if (showSplash) {
                    SplashScreen(
                        onSplashComplete = { 
                            Logger.logUIEvent("MainActivity", "SplashComplete", "Transitioning to auth check")
                            showSplash = false 
                        }
                    )
                } else {
                    if (isLoggedIn) {
                        Logger.logUIEvent("MainActivity", "MainScreenDisplayed", "User logged in - showing main screen")
                        MainScreen()
                    } else {
                        Logger.logUIEvent("MainActivity", "AuthScreenDisplayed", "User not logged in - showing auth flow")
                        SimpleAuthFlow(
                            onAuthComplete = { _ ->
                                Logger.logUIEvent("MainActivity", "AuthComplete", "User authenticated successfully")
                                
                                // API authentication - get customer data and refresh devices
                                coroutineScope.launch {
                                    try {
                                        val token = sessionManager.getAccessToken()
                                        if (token != null) {
                                            // Get customer profile
                                            val profileResult = customerRepository.getProfile(token)
                                            profileResult.fold(
                                                onSuccess = { response ->
                                                    sessionManager.updateCustomerData(response.data.customer)
                                                    Logger.i("Customer data updated in session", "AUTH")
                                                },
                                                onFailure = { exception ->
                                                    Logger.e("Failed to get customer profile", exception, "AUTH")
                                                }
                                            )
                                            
                                            // Refresh devices
                                            val refreshSuccess = deviceManager.refreshDevices(token)
                                            if (refreshSuccess) {
                                                Logger.i("Devices refreshed on app start", "AUTH")
                                            } else {
                                                Logger.w("Failed to refresh devices on app start", "AUTH")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Logger.e("Error updating customer data and devices", e, "AUTH")
                                    }
                                }
                                
                                isLoggedIn = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        PermissionManager.logPermissionStatus(this)
        
        val missingPermissions = PermissionManager.getAllMissingPermissions(this)
        if (missingPermissions.isNotEmpty()) {
            Logger.w("Requesting missing permissions: ${missingPermissions.joinToString(", ")}", "PERMISSIONS")
            permissionLauncher.launch(missingPermissions)
        } else {
            Logger.i("All required permissions already granted", "PERMISSIONS")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    IoTApplicationTheme {
        MainScreen()
    }
}