package com.ampush.iotapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ampush.iotapplication.data.manager.FcmTokenManager
import com.ampush.iotapplication.utils.PermissionManager
import com.ampush.iotapplication.utils.SessionManager
import kotlinx.coroutines.launch

@Composable
fun FcmDebugScreen() {
    val context = LocalContext.current
    val fcmTokenManager = remember { FcmTokenManager(context) }
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var fcmToken by remember { mutableStateOf("") }
    var tokenUpdated by remember { mutableStateOf(false) }
    var authToken by remember { mutableStateOf("") }
    var notificationPermission by remember { mutableStateOf(false) }
    var debugLogs by remember { mutableStateOf(listOf<String>()) }
    
    // Initialize debug info
    LaunchedEffect(Unit) {
        fcmToken = fcmTokenManager.getSavedFcmToken() ?: "No token"
        tokenUpdated = fcmTokenManager.isTokenUpdated()
        authToken = sessionManager.getAccessToken() ?: "No auth token"
        notificationPermission = PermissionManager.hasNotificationPermission(context)
        
        debugLogs = listOf(
            "FCM Token: ${fcmToken.take(30)}...",
            "Token Updated: $tokenUpdated",
            "Auth Token: ${authToken.take(30)}...",
            "Notification Permission: $notificationPermission"
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "FCM Debug Information",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Status Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                debugLogs.forEach { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        fcmTokenManager.refreshFcmToken()
                        fcmToken = fcmTokenManager.getSavedFcmToken() ?: "No token"
                        tokenUpdated = fcmTokenManager.isTokenUpdated()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Token"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh FCM Token")
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        fcmTokenManager.initializeFcmToken()
                        fcmToken = fcmTokenManager.getSavedFcmToken() ?: "No token"
                        tokenUpdated = fcmTokenManager.isTokenUpdated()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Initialize Token"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Initialize FCM")
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (tokenUpdated) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFFF9800).copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (tokenUpdated) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = "Status",
                    tint = if (tokenUpdated) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (tokenUpdated) "FCM Token Successfully Registered" else "FCM Token Pending Registration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (tokenUpdated) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
        }
    }
}
