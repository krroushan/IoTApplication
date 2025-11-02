package com.ampush.iotapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampush.iotapplication.data.db.entities.LogEntity
import com.ampush.iotapplication.data.manager.DeviceManager
import com.ampush.iotapplication.data.manager.DefaultDeviceManager
import com.ampush.iotapplication.data.model.Device
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ampush.iotapplication.ui.viewmodel.MotorViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.SmsDefaultChecker
import com.ampush.iotapplication.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    viewModel: MotorViewModel = viewModel(),
    refreshKey: Int = 0
) {
    val context = LocalContext.current
    val deviceManager = remember { DeviceManager(context) }
    val defaultDeviceManager = remember { DefaultDeviceManager(context) }
    val sessionManager = remember { SessionManager(context) }
    val smsDefaultChecker = remember { SmsDefaultChecker(context) }
    
    val latestLog by viewModel.latestLog.collectAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState(null)
    
    // Device selection state
    var showDeviceDialog by remember { mutableStateOf(false) }
    var pendingCommand by remember { mutableStateOf<String?>(null) }
    val savedDevices = remember { deviceManager.getSavedDevices() }
    
    // Refresh default device when refreshKey changes
    var defaultDevice by remember(refreshKey) { mutableStateOf(defaultDeviceManager.getDefaultDevice()) }
    
    // SMS sending state
    var isWaitingForSmsResponse by remember { mutableStateOf(false) }
    var lastSmsCommand by remember { mutableStateOf<String?>(null) }
    
    // SMS default check state
    var showSmsDefaultAlert by remember { mutableStateOf(false) }
    var smsDefaultInfo by remember { mutableStateOf<com.ampush.iotapplication.utils.SMSDefaultInfo?>(null) }
    
    // Helper function to send SMS and set waiting state
    val sendSmsWithWaiting = { device: Device, command: String ->
        deviceManager.sendSmsCommand(device, command)
        isWaitingForSmsResponse = true
        lastSmsCommand = command
        
        // Auto-clear waiting state after 30 seconds timeout
        viewModel.viewModelScope.launch {
            kotlinx.coroutines.delay(30000)
            isWaitingForSmsResponse = false
        }
    }
    
    // Listen for new log updates to clear waiting state
    LaunchedEffect(latestLog) {
        if (isWaitingForSmsResponse && latestLog != null) {
            isWaitingForSmsResponse = false
            lastSmsCommand = null
        }
    }
    
    // Check SMS default status when Dashboard loads
    LaunchedEffect(Unit) {
        try {
            val loggedInPhone = sessionManager.getUserPhone()
            if (loggedInPhone != null) {
                Logger.d("Checking SMS default status for: $loggedInPhone", "DASHBOARD")
                val info = smsDefaultChecker.getSmsDefaultInfo(loggedInPhone)
                smsDefaultInfo = info
                
                if (!info.isDefault) {
                    Logger.w("User's number is NOT default for SMS: ${info.message}", "DASHBOARD")
                    showSmsDefaultAlert = true
                } else {
                    Logger.i("User's number is default for SMS", "DASHBOARD")
                }
            }
        } catch (e: Exception) {
            Logger.e("Error checking SMS default status", e, "DASHBOARD")
        }
    }
    
    // Listen for new motor data broadcasts
    DisposableEffect(Unit) {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.ampush.iotapplication.NEW_MOTOR_DATA") {
                    Logger.d("Received new motor data broadcast", "DASHBOARD")
                    // Refresh the ViewModel to get latest data
                    viewModel.refreshLatestLog()
                }
            }
        }
        
        val filter = IntentFilter("com.ampush.iotapplication.NEW_MOTOR_DATA")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(broadcastReceiver, filter)
        }
        
        // Cleanup on dispose
        onDispose {
            try {
                context.unregisterReceiver(broadcastReceiver)
            } catch (e: Exception) {
                // Receiver might not be registered
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Motor Control Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Error Message
        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Control Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Motor Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Default device indicator
                    defaultDevice?.let { device ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Default: ${device.deviceName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            if (savedDevices.isEmpty()) {
                                viewModel.sendMotorOn() // Fallback to old behavior
                            } else {
                                val currentDefaultDevice = defaultDevice
                                if (currentDefaultDevice != null) {
                                    // Use default device
                                    sendSmsWithWaiting(currentDefaultDevice, "MOTORON")
                                } else {
                                    pendingCommand = "MOTORON"
                                    showDeviceDialog = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Motor ON")
                    }
                    
                    Button(
                        onClick = { 
                            if (savedDevices.isEmpty()) {
                                viewModel.sendMotorOff() // Fallback to old behavior
                            } else {
                                val currentDefaultDevice = defaultDevice
                                if (currentDefaultDevice != null) {
                                    // Use default device
                                    sendSmsWithWaiting(currentDefaultDevice, "MOTOROFF")
                                } else {
                                    pendingCommand = "MOTOROFF"
                                    showDeviceDialog = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("Motor OFF")
                    }
                }
                
                Button(
                    onClick = { 
                        if (savedDevices.isEmpty()) {
                            viewModel.sendStatusRequest() // Fallback to old behavior
                        } else {
                            val currentDefaultDevice = defaultDevice
                            if (currentDefaultDevice != null) {
                                // Use default device
                                sendSmsWithWaiting(currentDefaultDevice, "STATUS")
                            } else {
                                pendingCommand = "STATUS"
                                showDeviceDialog = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Get Status")
                }
            }
        }
        
        // Status Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Current Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Show waiting message when SMS command sent
                if (isWaitingForSmsResponse) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Column {
                                Text(
                                    text = "Waiting for SMS response...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                lastSmsCommand?.let { command ->
                                    Text(
                                        text = "Command: $command",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                
                latestLog?.let { log ->
                    StatusItem("Motor", log.motorStatus, getMotorStatusColor(log.motorStatus))
                    log.voltage?.let { StatusItem("Voltage", "${it}V", null) }
                    log.current?.let { StatusItem("Current", "${it}A", null) }
                    log.waterLevel?.let { StatusItem("Water Level", "${it}%", getWaterLevelColor(it)) }
                    log.mode?.let { StatusItem("Mode", it, null) }
                    log.clock?.let { StatusItem("Clock", it, null) }
                    log.runTime?.let { StatusItem("Run Time", "${it}s", Color(0xFF2196F3)) }
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    StatusItem("Last Update", dateFormat.format(log.timestamp), null)
                } ?: run {
                    Text(
                        text = "No status data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Loading Indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    
    // Device Selection Dialog
    if (showDeviceDialog) {
        DeviceSelectionDialog(
            devices = savedDevices,
            onDeviceSelected = { device ->
                pendingCommand?.let { command ->
                    sendSmsWithWaiting(device, command)
                }
                showDeviceDialog = false
                pendingCommand = null
            },
            onDismiss = {
                showDeviceDialog = false
                pendingCommand = null
            }
        )
    }
    
    // SMS Default Alert Dialog
    if (showSmsDefaultAlert && smsDefaultInfo != null) {
        AlertDialog(
            onDismissRequest = { showSmsDefaultAlert = false },
            title = {
                Text(
                    text = "SMS Default Setting",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your logged-in number (${smsDefaultInfo!!.loggedInNumber}) is not set as the default SIM for SMS sending.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Current default SMS number: ${smsDefaultInfo!!.defaultSmsNumber ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "To ensure SMS commands are sent correctly, please set your number as the default SIM for SMS in your device settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSmsDefaultAlert = false }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showSmsDefaultAlert = false
                        // Optionally open device settings
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Logger.e("Could not open settings", e, "DASHBOARD")
                        }
                    }
                ) {
                    Text("Open Settings")
                }
            }
        )
    }
}

@Composable
private fun StatusItem(
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun getMotorStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "ON" -> Color(0xFF4CAF50)
        "OFF" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

private fun getWaterLevelColor(level: Float): Color {
    return when {
        level < 20 -> Color(0xFFF44336) // Red
        level < 50 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFF4CAF50) // Green
    }
}
