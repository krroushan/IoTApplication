package com.ampush.iotapplication.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.ampush.iotapplication.repository.CustomerRepository
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.SessionManager
import com.ampush.iotapplication.utils.PermissionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FcmTokenManager(private val context: Context) {
    
    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
    
    private val customerRepository = CustomerRepository()
    private val sessionManager = SessionManager(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_TOKEN_UPDATED = "token_updated"
    }
    
    /**
     * Initialize FCM token and register with server
     */
    fun initializeFcmToken() {
        Logger.d("=== FCM Token Initialization Started ===", "FCM_MANAGER")
        coroutineScope.launch {
            try {
                // Check notification permission first
                if (!PermissionManager.hasNotificationPermission(context)) {
                    Logger.w("Notification permission not granted, FCM token may not work properly", "FCM_MANAGER")
                } else {
                    Logger.d("Notification permission granted", "FCM_MANAGER")
                }
                
                // Check if user is logged in
                val authToken = sessionManager.getAccessToken()
                if (authToken == null) {
                    Logger.w("No auth token available - user not logged in yet", "FCM_MANAGER")
                    Logger.w("FCM token will be sent to server after login", "FCM_MANAGER")
                } else {
                    Logger.d("Auth token available: ${authToken.take(20)}...", "FCM_MANAGER")
                }
                
                // Get FCM token from Firebase
                Logger.d("Requesting FCM token from Firebase...", "FCM_MANAGER")
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                
                if (fcmToken != null) {
                    Logger.i("✅ FCM token obtained: ${fcmToken.take(20)}...", "FCM_MANAGER")
                    
                    // Check if token has changed
                    val savedToken = getSavedFcmToken()
                    Logger.d("Saved token: ${savedToken?.take(20) ?: "null"}...", "FCM_MANAGER")
                    
                    if (savedToken != fcmToken) {
                        Logger.d("FCM token changed, updating server", "FCM_MANAGER")
                        updateFcmTokenOnServer(fcmToken)
                    } else {
                        Logger.d("FCM token unchanged, skipping update", "FCM_MANAGER")
                    }
                } else {
                    Logger.e("❌ Failed to get FCM token from Firebase", null, "FCM_MANAGER")
                }
            } catch (e: Exception) {
                Logger.e("❌ Error initializing FCM token", e, "FCM_MANAGER")
                Logger.e("Exception: ${e.javaClass.simpleName}: ${e.message}", null, "FCM_MANAGER")
            }
        }
    }
    
           /**
            * Update FCM token on server
            */
           private suspend fun updateFcmTokenOnServer(fcmToken: String) {
               try {
                   val authToken = sessionManager.getAccessToken()
                   if (authToken != null) {
                       Logger.d("Attempting to update FCM token on server", "FCM_MANAGER")
                       Logger.d("Auth token available: ${authToken.take(20)}...", "FCM_MANAGER")
                       Logger.d("FCM token: ${fcmToken.take(20)}...", "FCM_MANAGER")

                       val result = customerRepository.updateFcmToken(authToken, fcmToken)

                       result.fold(
                           onSuccess = { response ->
                               saveFcmToken(fcmToken)
                               Logger.i("FCM token updated on server successfully", "FCM_MANAGER")
                               Logger.d("Server response: ${response.message}", "FCM_MANAGER")
                           },
                           onFailure = { exception ->
                               Logger.e("Failed to update FCM token on server", exception, "FCM_MANAGER")
                               Logger.e("Error details: ${exception.message}", null, "FCM_MANAGER")
                               
                               // Check if it's a JSON parsing error (API returning HTML)
                               if (exception.message?.contains("MalformedJsonException") == true) {
                                   Logger.w("API endpoint returning HTML instead of JSON - backend API not implemented", "FCM_MANAGER")
                                   Logger.w("Saving FCM token locally anyway for future use", "FCM_MANAGER")
                                   saveFcmToken(fcmToken)
                               }
                           }
                       )
                   } else {
                       Logger.w("No auth token available, cannot update FCM token", "FCM_MANAGER")
                       Logger.w("User may not be logged in", "FCM_MANAGER")
                   }
               } catch (e: Exception) {
                   Logger.e("Error updating FCM token on server", e, "FCM_MANAGER")
                   Logger.e("Exception details: ${e.message}", null, "FCM_MANAGER")
                   
                   // Check if it's a JSON parsing error (API returning HTML)
                   if (e.message?.contains("MalformedJsonException") == true) {
                       Logger.w("API endpoint returning HTML instead of JSON - backend API not implemented", "FCM_MANAGER")
                       Logger.w("Saving FCM token locally anyway for future use", "FCM_MANAGER")
                       saveFcmToken(fcmToken)
                   }
               }
           }
    
    /**
     * Save FCM token locally
     */
    private fun saveFcmToken(fcmToken: String) {
        sharedPrefs.edit()
            .putString(KEY_FCM_TOKEN, fcmToken)
            .putBoolean(KEY_TOKEN_UPDATED, true)
            .apply()
    }
    
    /**
     * Get saved FCM token
     */
    fun getSavedFcmToken(): String? {
        return sharedPrefs.getString(KEY_FCM_TOKEN, null)
    }
    
    /**
     * Check if FCM token was updated
     */
    fun isTokenUpdated(): Boolean {
        return sharedPrefs.getBoolean(KEY_TOKEN_UPDATED, false)
    }
    
    /**
     * Clear FCM token (for logout)
     */
    fun clearFcmToken() {
        sharedPrefs.edit()
            .remove(KEY_FCM_TOKEN)
            .remove(KEY_TOKEN_UPDATED)
            .apply()
    }
    
    /**
     * Force refresh FCM token
     */
    fun refreshFcmToken() {
        coroutineScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                if (fcmToken != null) {
                    updateFcmTokenOnServer(fcmToken)
                }
            } catch (e: Exception) {
                Logger.e("Error refreshing FCM token", e, "FCM_MANAGER")
            }
        }
    }
}
