package com.ampush.iotapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampush.iotapplication.data.db.entities.LogEntity
import com.ampush.iotapplication.data.manager.DeviceManager
import com.ampush.iotapplication.data.model.Device
import com.ampush.iotapplication.network.models.MotorLogEntity
import com.ampush.iotapplication.repository.DeviceLogsRepository
import com.ampush.iotapplication.ui.viewmodel.MotorViewModel
import com.ampush.iotapplication.ui.components.DeviceStatsSummary
import com.ampush.iotapplication.ui.components.DeviceHistoryItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: MotorViewModel = viewModel()
) {
    val context = LocalContext.current
    val deviceManager = remember { DeviceManager(context) }
    val savedDevices = remember { deviceManager.getSavedDevices() }
    val allLogs by viewModel.getAllLogs().collectAsState(initial = emptyList())
    
    // Create tabs - "Local" + device names
    val tabs = remember(savedDevices) {
        listOf("Local") + savedDevices.map { it.deviceName }
    }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Tabs
        if (tabs.size > 1) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, tabName ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tabName,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }
        
        // Content based on selected tab
        when {
            selectedTabIndex == 0 -> {
                // Local tab - show SQLite data (unchanged)
                LocalHistoryContent(allLogs = allLogs)
            }
            selectedTabIndex > 0 && selectedTabIndex <= savedDevices.size -> {
                // Device tab - show API data for specific device
                val selectedDevice = savedDevices[selectedTabIndex - 1]
                DeviceHistoryContent(device = selectedDevice)
            }
        }
    }
}

@Composable
private fun LocalHistoryContent(allLogs: List<LogEntity>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Summary
        if (allLogs.isNotEmpty()) {
            StatsSummary(logs = allLogs)
        }
        
        // History List
        if (allLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No local history data available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allLogs) { log ->
                    HistoryItem(log = log)
                }
            }
        }
    }
}

@Composable
private fun DeviceHistoryContent(device: Device) {
    val deviceLogsRepository = remember { DeviceLogsRepository() }
    var deviceLogs by remember { mutableStateOf<List<MotorLogEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load device logs when component is first composed
    LaunchedEffect(device.id) {
        isLoading = true
        errorMessage = null
        
        val result = deviceLogsRepository.getDeviceLogs(deviceId = device.id)
        result.fold(
            onSuccess = { logs ->
                deviceLogs = logs
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message ?: "Failed to load device logs"
                isLoading = false
            }
        )
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "SMS: ${device.smsNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                device.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Device logs
        if (!isLoading && errorMessage == null) {
            if (deviceLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No API logs available for ${device.deviceName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Stats Summary for device logs
                DeviceStatsSummary(logs = deviceLogs)
                
                // Device logs list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(deviceLogs) { log ->
                        DeviceHistoryItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSummary(logs: List<LogEntity>) {
    val onCount = logs.count { it.motorStatus.uppercase() == "ON" }
    val offCount = logs.count { it.motorStatus.uppercase() == "OFF" }
    val avgVoltage = logs.mapNotNull { it.voltage }.average().let { 
        if (it.isNaN()) null else it.toFloat() 
    }
    val avgWaterLevel = logs.mapNotNull { it.waterLevel }.average().let { 
        if (it.isNaN()) null else it.toFloat() 
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("ON", onCount.toString(), Color(0xFF4CAF50))
                StatItem("OFF", offCount.toString(), Color(0xFFF44336))
                avgVoltage?.let { 
                    StatItem("Avg V", String.format("%.1f", it), Color.Blue)
                }
                avgWaterLevel?.let { 
                    StatItem("Avg WL", String.format("%.1f%%", it), Color(0xFF2196F3))
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryItem(log: LogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.command,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Surface(
                    modifier = Modifier.padding(4.dp),
                    shape = MaterialTheme.shapes.small,
                    color = when (log.motorStatus.uppercase()) {
                        "ON" -> Color(0xFF4CAF50)
                        "OFF" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                ) {
                    Text(
                        text = log.motorStatus,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Text(
                text = dateFormat.format(log.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Additional data if available
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // First row - Voltage, Current, Water Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    log.voltage?.let { 
                        Text(
                            text = "V: ${it}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    log.current?.let { 
                        Text(
                            text = "A: ${it}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    log.waterLevel?.let { 
                        Text(
                            text = "WL: ${it}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Second row - Mode, Clock, Run Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    log.mode?.let { 
                        Text(
                            text = "Mode: ${it}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    log.clock?.let { 
                        Text(
                            text = "Clock: ${it}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    log.runTime?.let { 
                        Text(
                            text = "Run: ${it}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Third row - Phone Number and Sync Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Phone: ${log.phoneNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = if (log.isSynced) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    ) {
                        Text(
                            text = if (log.isSynced) "Synced" else "Pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
