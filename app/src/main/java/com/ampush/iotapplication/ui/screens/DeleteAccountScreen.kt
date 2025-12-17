package com.ampush.iotapplication.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ampush.iotapplication.MainActivity
import com.ampush.iotapplication.data.manager.DeviceManager
import com.ampush.iotapplication.data.manager.FcmTokenManager
import com.ampush.iotapplication.repository.CustomerRepository
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.SessionManager
import kotlinx.coroutines.launch

@Composable
fun DeleteAccountScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val customerRepository = remember { CustomerRepository() }
    val deviceManager = remember { DeviceManager(context) }
    val fcmTokenManager = remember { FcmTokenManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val requiredConfirmation = "DELETE MY ACCOUNT"
    val isFormValid = password.isNotBlank() && confirmation == requiredConfirmation
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Delete Account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ampush Motor Controller",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This action cannot be undone",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "What will be deleted:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val items = listOf(
                    "Your account and profile information",
                    "All API tokens (you'll be logged out)",
                    "Your profile photo",
                    "All device assignments",
                    "All notifications"
                )
                
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Note: Motor logs will remain in the system for historical data purposes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "By AMPUSHWORKS ENTERPRISES PRIVATE LIMITED",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Confirm Deletion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = errorMessage != null && password.isNotBlank()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirmation Field
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = {
                        confirmation = it
                        errorMessage = null
                    },
                    label = { Text("Type: DELETE MY ACCOUNT") },
                    placeholder = { Text("Type: DELETE MY ACCOUNT") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = confirmation.isNotBlank() && confirmation != requiredConfirmation,
                    supportingText = {
                        if (confirmation.isNotBlank() && confirmation != requiredConfirmation) {
                            Text(
                                text = "Must match exactly: $requiredConfirmation",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                
                // Error Message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        // Delete Button
        Button(
            onClick = {
                if (isFormValid) {
                    showConfirmDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onError
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deleting Account...")
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete My Account",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Cancel Button
        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cancel")
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Final Confirmation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you absolutely sure you want to delete your Ampush Motor Controller account?",
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action is permanent and cannot be undone. All your data with AMPUSHWORKS ENTERPRISES PRIVATE LIMITED will be permanently deleted.",
                        textAlign = TextAlign.Start
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        isLoading = true
                        errorMessage = null
                        
                        coroutineScope.launch {
                            try {
                                val token = sessionManager.getAccessToken()
                                if (token == null) {
                                    errorMessage = "You are not logged in"
                                    isLoading = false
                                    return@launch
                                }
                                
                                val result = customerRepository.deleteAccount(token, password)
                                
                                result.fold(
                                    onSuccess = { response ->
                                        Logger.i("Account deleted successfully", "DELETE_ACCOUNT")
                                        
                                        // Clear all local data
                                        deviceManager.clearDevices()
                                        fcmTokenManager.clearFcmToken()
                                        sessionManager.logout()
                                        
                                        // Show success message and restart app
                                        isLoading = false
                                        
                                        // Restart activity to go to login screen
                                        // recreate() will check session state and show login screen
                                        (context as? android.app.Activity)?.recreate()
                                    },
                                    onFailure = { exception ->
                                        Logger.e("Failed to delete account", exception, "DELETE_ACCOUNT")
                                        isLoading = false
                                        
                                        // Parse error message
                                        val errorMsg = when {
                                            exception.message?.contains("401") == true -> "Invalid password"
                                            exception.message?.contains("422") == true -> "Validation failed. Please check your password and confirmation."
                                            exception.message?.contains("500") == true -> "Server error. Please try again later."
                                            else -> exception.message ?: "Failed to delete account. Please try again."
                                        }
                                        errorMessage = errorMsg
                                    }
                                )
                            } catch (e: Exception) {
                                Logger.e("Error deleting account", e, "DELETE_ACCOUNT")
                                isLoading = false
                                errorMessage = "An error occurred: ${e.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

