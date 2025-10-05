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
import com.ampush.iotapplication.data.model.Device
import com.ampush.iotapplication.ui.viewmodel.MotorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    viewModel: MotorViewModel = viewModel()
) {
    val context = LocalContext.current
    val deviceManager = remember { DeviceManager(context) }
    
    val latestLog by viewModel.latestLog.collectAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState(null)
    
    // Device selection state
    var showDeviceDialog by remember { mutableStateOf(false) }
    var pendingCommand by remember { mutableStateOf<String?>(null) }
    val savedDevices = remember { deviceManager.getSavedDevices() }
    
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
                Text(
                    text = "Motor Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            if (savedDevices.isEmpty()) {
                                viewModel.sendMotorOn() // Fallback to old behavior
                            } else {
                                pendingCommand = "MOTORON"
                                showDeviceDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isLoading != true,
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
                                pendingCommand = "MOTOROFF"
                                showDeviceDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isLoading != true,
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
                            pendingCommand = "STATUS"
                            showDeviceDialog = true
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
                    deviceManager.sendSmsCommand(device, command)
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
