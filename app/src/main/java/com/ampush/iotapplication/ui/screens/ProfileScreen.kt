package com.ampush.iotapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.ampush.iotapplication.utils.SessionManager
import com.ampush.iotapplication.repository.CustomerRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.compose.AsyncImagePainter
import androidx.compose.ui.res.painterResource
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.data.model.Device
import com.ampush.iotapplication.data.manager.DeviceManager
import com.ampush.iotapplication.data.manager.FcmTokenManager
import com.ampush.iotapplication.utils.PermissionManager
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.LazyListState
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val customerRepository = remember { CustomerRepository() }
    val deviceManager = remember { DeviceManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    
    // Track if profile header is visible for lazy loading
    val isProfileHeaderVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(sessionManager, isProfileHeaderVisible)
        }
        
        // Customer Information
        item {
            CustomerInfoSection(sessionManager)
        }
        
        // Devices Section
        item {
            DevicesSection(sessionManager, context)
        }
        
        // About Section
        item {
            AboutSection(sessionManager, context, coroutineScope, customerRepository)
        }
    }
}

@Composable
private fun ProfileHeader(sessionManager: SessionManager, isVisible: Boolean = true) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            val profilePhotoUrl = sessionManager.getCustomerProfilePhoto()
            Logger.d("Profile photo URL: $profilePhotoUrl", "PROFILE_IMAGE")
            
            if (profilePhotoUrl != null && isVisible) {
                Logger.d("Attempting to load image with lazy loading (visible: $isVisible)", "PROFILE_IMAGE")
                
                // Test URL accessibility only when visible
                LaunchedEffect(profilePhotoUrl, isVisible) {
                    if (isVisible) {
                        testImageUrl(profilePhotoUrl)
                    }
                }
                
                // Lazy loading with custom ImageRequest - only load when visible
                val context = LocalContext.current
                val imageRequest = remember(profilePhotoUrl, isVisible) {
                    if (isVisible) {
                        ImageRequest.Builder(context)
                            .data(profilePhotoUrl)
                            .memoryCacheKey(profilePhotoUrl)
                            .diskCacheKey(profilePhotoUrl)
                            .build()
                    } else {
                        null
                    }
                }
                
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        error = painterResource(id = android.R.drawable.ic_menu_gallery),
                        onError = { 
                            Logger.e("Lazy loading failed for profile image: $profilePhotoUrl", null, "PROFILE_IMAGE")
                        },
                        onSuccess = {
                            Logger.i("Lazy loading successful for profile image: $profilePhotoUrl", "PROFILE_IMAGE")
                        }
                    )
                } else {
                    // Show placeholder when not visible
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Logger.w("No profile photo URL available", "PROFILE_IMAGE")
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = sessionManager.getUserName() ?: "User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = sessionManager.getCustomerEmail() ?: "No email available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Show additional customer info if available
            if (sessionManager.hasApiToken()) {
                val address = sessionManager.getCustomerAddress()
                val city = sessionManager.getCustomerCity()
                val state = sessionManager.getCustomerState()
                if (address != null && city != null && state != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$address, $city, $state",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Badge
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                color = Color(0xFFE8F5E8)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerInfoSection(sessionManager: SessionManager) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Customer Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone Number
            CustomerInfoRow(
                icon = Icons.Default.Phone,
                label = "Phone Number",
                value = sessionManager.getUserPhone() ?: "Not available"
            )
            
            // Email
            CustomerInfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = sessionManager.getCustomerEmail() ?: "Not available"
            )
            
            // Address
            val address = sessionManager.getCustomerAddress()
            val city = sessionManager.getCustomerCity()
            val state = sessionManager.getCustomerState()
            val postalCode = sessionManager.getCustomerPostalCode()
            val country = sessionManager.getCustomerCountry()
            
            if (address != null) {
                CustomerInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Address",
                    value = buildString {
                        append(address)
                        if (city != null) append(", $city")
                        if (state != null) append(", $state")
                        if (postalCode != null) append(" - $postalCode")
                        if (country != null) append(", $country")
                    }
                )
            }
            
            // Profile Photo
            val profilePhotoUrl = sessionManager.getCustomerProfilePhoto()
            if (profilePhotoUrl != null) {
                CustomerInfoRow(
                    icon = Icons.Default.Person,
                    label = "Profile Photo",
                    value = "Available"
                )
            }
        }
    }
}

@Composable
private fun CustomerInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AboutItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AboutSection(
    sessionManager: SessionManager, 
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    customerRepository: CustomerRepository
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AboutItem(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = "1.0.0 (Build 1)"
            )
            
            AboutItem(
                icon = Icons.Default.Info,
                title = "Help & Support",
                subtitle = "FAQ, contact us"
            )
            
            AboutItem(
                icon = Icons.Default.Lock,
                title = "Privacy Policy",
                subtitle = "How we handle your data"
            )
            
            AboutItem(
                icon = Icons.Default.Info,
                title = "Terms of Service",
                subtitle = "Legal terms and conditions"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout Button
            Button(
                onClick = {
                    // API logout
                    coroutineScope.launch {
                        try {
                            val token = sessionManager.getAccessToken()
                            if (token != null) {
                                customerRepository.logout(token)
                            }
                        } catch (e: Exception) {
                            // Log error but continue with local logout
                            com.ampush.iotapplication.utils.Logger.e("API logout failed", e, "PROFILE")
                        } finally {
                            // Clear devices
                            val deviceManager = DeviceManager(context)
                            deviceManager.clearDevices()
                            // Always perform local logout
                            sessionManager.logout()
                            // Restart app to go back to login
                            (context as? android.app.Activity)?.recreate()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
private fun DevicesSection(sessionManager: SessionManager, context: android.content.Context) {
    val deviceManager = remember { DeviceManager(context) }
    val fcmTokenManager = remember { FcmTokenManager(context) }
    val customerRepository = remember { CustomerRepository() }
    val coroutineScope = rememberCoroutineScope()
    var devices by remember { mutableStateOf(deviceManager.getSavedDevices()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var fcmTokenStatus by remember { mutableStateOf(fcmTokenManager.isTokenUpdated()) }
    var fcmTokenDebug by remember { mutableStateOf("") }
    
    // Refresh devices function
    val refreshDevices: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            try {
                val token = sessionManager.getAccessToken()
                if (token != null) {
                    val refreshSuccess = deviceManager.refreshDevices(token)
                       if (refreshSuccess) {
                           devices = deviceManager.getSavedDevices()
                           fcmTokenStatus = fcmTokenManager.isTokenUpdated()
                           Logger.i("Devices refreshed from profile screen", "PROFILE")
                       }
                }
            } catch (e: Exception) {
                Logger.e("Error refreshing devices from profile", e, "PROFILE")
            } finally {
                isRefreshing = false
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
                   Row(
                       modifier = Modifier.fillMaxWidth(),
                       horizontalArrangement = Arrangement.SpaceBetween,
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Column {
                           Text(
                               text = "My Devices",
                               style = MaterialTheme.typography.titleMedium,
                               fontWeight = FontWeight.Bold
                           )
                           // FCM Status indicator
                           Row(
                               verticalAlignment = Alignment.CenterVertically
                           ) {
                               val hasNotificationPermission = PermissionManager.hasNotificationPermission(context)
                               val statusIcon = when {
                                   !hasNotificationPermission -> Icons.Default.Warning
                                   fcmTokenStatus -> Icons.Default.CheckCircle
                                   else -> Icons.Default.Info
                               }
                               val statusColor = when {
                                   !hasNotificationPermission -> Color(0xFFFF5722)
                                   fcmTokenStatus -> Color(0xFF4CAF50)
                                   else -> Color(0xFFFF9800)
                               }
                               val statusText = when {
                                   !hasNotificationPermission -> "Permission Required"
                                   fcmTokenStatus -> "Notifications Active"
                                   else -> "Notifications Pending"
                               }
                               
                               Icon(
                                   imageVector = statusIcon,
                                   contentDescription = "FCM Status",
                                   modifier = Modifier.size(12.dp),
                                   tint = statusColor
                               )
                               Spacer(modifier = Modifier.width(4.dp))
                               Column {
                                   Text(
                                       text = statusText,
                                       style = MaterialTheme.typography.bodySmall,
                                       color = statusColor
                                   )
                                   if (fcmTokenDebug.isNotEmpty()) {
                                       Text(
                                           text = fcmTokenDebug,
                                           style = MaterialTheme.typography.bodySmall,
                                           color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                       )
                                   }
                               }
                           }
                       }

                       Row {
                           // FCM Debug Button
                           IconButton(
                               onClick = {
                                   // For now, just log the debug button click
                                   // Navigation will be handled by the parent composable
                                   Logger.d("FCM Debug button clicked", "PROFILE")
                               }
                           ) {
                           Icon(
                               imageVector = Icons.Default.Settings,
                               contentDescription = "FCM Debug"
                           )
                           }
                           
                           // FCM Token Refresh Button
                           IconButton(
                               onClick = {
                                   coroutineScope.launch {
                                       fcmTokenManager.refreshFcmToken()
                                       fcmTokenStatus = fcmTokenManager.isTokenUpdated()
                                       fcmTokenDebug = "Token: ${fcmTokenManager.getSavedFcmToken()?.take(20) ?: "None"}..."
                                   }
                               }
                           ) {
                               Icon(
                                   imageVector = Icons.Default.Notifications,
                                   contentDescription = "Refresh FCM Token"
                               )
                           }
                           
                           // Devices Refresh Button
                           IconButton(
                               onClick = refreshDevices,
                               enabled = !isRefreshing
                           ) {
                               if (isRefreshing) {
                                   CircularProgressIndicator(
                                       modifier = Modifier.size(20.dp),
                                       strokeWidth = 2.dp
                                   )
                               } else {
                                   Icon(
                                       imageVector = Icons.Default.Refresh,
                                       contentDescription = "Refresh Devices"
                                   )
                               }
                           }
                       }
                   }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (devices.isEmpty()) {
                // No devices message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "No devices",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No devices assigned",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Contact administrator to assign devices",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Devices list
                devices.forEach { device ->
                    DeviceItem(device = device)
                    if (device != devices.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(device: Device) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Device Name
            Text(
                text = device.deviceName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // SMS Number
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "SMS Number",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = device.smsNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Description (if available)
            device.description?.let { description ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Last Activity (if available)
            device.lastActivityAt?.let { lastActivity ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Last Activity",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last activity: $lastActivity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private suspend fun testImageUrl(url: String) {
    try {
        Logger.d("Testing URL accessibility: $url", "PROFILE_IMAGE")
        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            val contentType = connection.contentType
            val contentLength = connection.contentLength
            
            Logger.d("URL test result - Response Code: $responseCode, Content-Type: $contentType, Content-Length: $contentLength", "PROFILE_IMAGE")
            
            if (responseCode == 200) {
                Logger.i("Image URL is accessible", "PROFILE_IMAGE")
            } else {
                Logger.w("Image URL returned non-200 response: $responseCode", "PROFILE_IMAGE")
            }
            
            connection.disconnect()
        }
    } catch (e: Exception) {
        Logger.e("Failed to test image URL accessibility", e, "PROFILE_IMAGE")
    }
}
