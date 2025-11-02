package com.ampush.iotapplication.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.repository.CustomerRepository
import com.ampush.iotapplication.utils.SessionManager
import com.ampush.iotapplication.data.manager.DeviceManager
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.ampush.iotapplication.utils.SimPhoneDetector
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import com.ampush.iotapplication.utils.PermissionHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.ampush.iotapplication.repository.PhoneValidationRepository
import com.ampush.iotapplication.network.models.PhoneValidationResponse

@Composable
fun SimpleLoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var detectedPhoneNumber by remember { mutableStateOf<String?>(null) }
    var allDetectedSims by remember { mutableStateOf<List<com.ampush.iotapplication.utils.SimInfo>>(emptyList()) }
    var showSimDetection by remember { mutableStateOf(false) }
    var showSimSelection by remember { mutableStateOf(false) }
    var isValidatingPhone by remember { mutableStateOf(false) }
    var phoneValidationResult by remember { mutableStateOf<PhoneValidationResponse?>(null) }
    
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val customerRepository = remember { CustomerRepository() }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val deviceManager = remember { DeviceManager(context) }
    val simDetector = remember { SimPhoneDetector(context) }
    val phoneValidationRepository = remember { PhoneValidationRepository() }
    
    // Function to validate phone number
    fun validatePhoneNumber(phone: String) {
        coroutineScope.launch {
            isValidatingPhone = true
            phoneValidationResult = null
            errorMessage = null
            
            try {
                val result = phoneValidationRepository.validatePhoneNumber(phone)
                result.fold(
                    onSuccess = { validationResponse ->
                        phoneValidationResult = validationResponse
                        if (validationResponse.isRegistered) {
                            Logger.i("Phone number $phone is registered", "PHONE_VALIDATION")
                            // Clear any previous error messages when phone is registered
                            errorMessage = null
                        } else {
                            Logger.w("Phone number $phone is not registered: ${validationResponse.message}", "PHONE_VALIDATION")
                            // Show the API message for unregistered numbers
                            errorMessage = validationResponse.message
                        }
                    },
                    onFailure = { exception ->
                        Logger.e("Phone validation failed for $phone", exception, "PHONE_VALIDATION")
                        errorMessage = "Failed to validate phone number. Please try again."
                    }
                )
            } catch (e: Exception) {
                Logger.e("Exception during phone validation", e, "PHONE_VALIDATION")
                errorMessage = "Network error. Please check your connection."
            } finally {
                isValidatingPhone = false
            }
        }
    }
    
    // Permission launcher for all essential permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Logger.i("All essential permissions granted", "PERMISSIONS")
            // Retry SIM detection after permissions granted
            coroutineScope.launch {
                try {
                    val allSims = simDetector.getAllSimInfo()
                    allDetectedSims = allSims
                    if (allSims.isNotEmpty()) {
                        Logger.i("Auto-detected ${allSims.size} SIM(s) after permissions", "SIM_DETECTION")
                        // Don't prefill phone number - let user choose
                        if (allSims.size > 1) {
                            showSimSelection = true
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("Error auto-detecting phone numbers after permissions", e, "SIM_DETECTION")
                }
            }
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys
            Logger.w("Some permissions denied: $deniedPermissions", "PERMISSIONS")
            errorMessage = "Some permissions are required for the app to function properly"
        }
    }
    
    // Auto-request all essential permissions on first load
    LaunchedEffect(Unit) {
        try {
            if (PermissionHelper.hasAllEssentialPermissions(context)) {
                // All permissions already granted, detect SIMs immediately
                Logger.i("All essential permissions already granted", "PERMISSIONS")
                val allSims = simDetector.getAllSimInfo()
                allDetectedSims = allSims
                if (allSims.isNotEmpty()) {
                    Logger.i("Auto-detected ${allSims.size} SIM(s): ${allSims.map { it.phoneNumber }}", "SIM_DETECTION")
                    // Don't prefill phone number - let user choose
                    if (allSims.size > 1) {
                        showSimSelection = true
                    }
                }
            } else {
                // Request all essential permissions automatically
                Logger.d("Requesting all essential permissions automatically", "PERMISSIONS")
                val permissions = PermissionHelper.getAllEssentialPermissions()
                Logger.d("Requesting permissions: ${permissions.joinToString(", ")}", "PERMISSIONS")
                permissionLauncher.launch(permissions)
            }
        } catch (e: Exception) {
            Logger.e("Error requesting permissions", e, "PERMISSIONS")
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .imePadding(), // Keyboard avoiding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "IoT Motor Control",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            
            Text(
                text = "Sign in to control your devices",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            //Spacer(modifier = Modifier.height(32.dp))
            
            // API Info Card
            
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Multiple SIMs Detected Section (ABOVE phone input)
                    if (allDetectedSims.isNotEmpty() && allDetectedSims.size > 1) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = "SIM Cards",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Multiple SIMs Detected (${allDetectedSims.size})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                allDetectedSims.forEachIndexed { index, simInfo ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                val selectedNumber = simInfo.phoneNumber ?: ""
                                                phoneNumber = selectedNumber
                                                validatePhoneNumber(selectedNumber)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (phoneNumber == simInfo.phoneNumber) 
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "SIM ${index + 1}:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = simInfo.phoneNumber ?: "No number",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            
                                            // Validation status indicator
                                            if (isValidatingPhone && phoneNumber == simInfo.phoneNumber) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else if (phoneValidationResult != null && phoneNumber == simInfo.phoneNumber) {
                                                Icon(
                                                    imageVector = if (phoneValidationResult!!.isRegistered) 
                                                        Icons.Default.CheckCircle 
                                                    else Icons.Default.Warning,
                                                    contentDescription = "Validation status",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (phoneValidationResult!!.isRegistered) 
                                                        Color(0xFF4CAF50) 
                                                    else Color(0xFFF44336)
                                                )
                                            }
                                            
                                            simInfo.carrierName?.let { carrier ->
                                                Text(
                                                    text = carrier,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                // Red warning message
                                Text(
                                    text = "Select the SIM card you want to use",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        //Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Phone Number Input (BELOW SIM selection)
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Phone")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("+91 9876543210") }
                    )
                    
                    // Single SIM detected (show below phone input)
                    if (allDetectedSims.isNotEmpty() && allDetectedSims.size == 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    val selectedNumber = allDetectedSims.first().phoneNumber ?: ""
                                    phoneNumber = selectedNumber
                                    validatePhoneNumber(selectedNumber)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "SIM Card",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detected: ${allDetectedSims.first().phoneNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (isValidatingPhone) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Tap to use",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password")
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (phoneNumber.isNotBlank() && password.isNotBlank()) {
                                    // API login will be handled in the button click
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Validation Status Messages
                    if (phoneValidationResult != null && phoneValidationResult!!.isRegistered) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Valid",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✓ Phone number is registered",
                                    color = Color(0xFF4CAF50),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Error Message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // Login Button
                    Button(
                        onClick = {
                            if (phoneNumber.isNotBlank() && password.isNotBlank()) {
                                isLoading = true
                                errorMessage = null
                                
                                // API Authentication
                                coroutineScope.launch {
                                    try {
                                        val result = customerRepository.login(phoneNumber, password)
                                        
                                        result.fold(
                                            onSuccess = { response ->
                                                Logger.i("API login successful: ${response.data.customer.phoneNumber}", "AUTH")
                                                // Save API session
                                                sessionManager.saveApiSession(
                                                    response.data.customer,
                                                    response.data.token,
                                                    response.data.tokenType
                                                )
                                                // Save devices
                                                deviceManager.saveDevices(response.data.devices)
                                                Logger.i("Saved ${response.data.devices.size} devices", "AUTH")
                                                // Pass customer data to success callback
                                                onLoginSuccess(response.data.customer.phoneNumber)
                                            },
                                            onFailure = { exception ->
                                                isLoading = false
                                                errorMessage = exception.message ?: "Login failed"
                                                Logger.e("API login failed", exception, "AUTH")
                                            }
                                        )
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = "Network error. Please try again."
                                        Logger.e("API login network error", e, "AUTH")
                                    }
                                }
                            } else {
                                errorMessage = "Please enter phone number and password"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In")
                        }
                    }
                    
                }
            }
            
        }
    }
}
