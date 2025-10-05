package com.ampush.iotapplication.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ampush.iotapplication.network.RetrofitClient
import com.ampush.iotapplication.network.models.DashboardSummaryResponse
import com.ampush.iotapplication.repository.ReportRepository
import com.ampush.iotapplication.utils.SessionManager
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val apiService = remember { RetrofitClient.apiService }
    val reportRepository = remember { ReportRepository(apiService, sessionManager) }
    val coroutineScope = rememberCoroutineScope()
    
    var summaryReport by remember { mutableStateOf<DashboardSummaryResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val result = reportRepository.getSummaryReport(days = 7)
        result.fold(
            onSuccess = { report ->
                summaryReport = report
                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = exception.message ?: "Failed to load dashboard"
                isLoading = false
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Dashboard",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Motor Control Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading dashboard...")
                    }
                }
            }
            
            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error Loading Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    val result = reportRepository.getSummaryReport(days = 7)
                                    result.fold(
                                        onSuccess = { report ->
                                            summaryReport = report
                                            isLoading = false
                                        },
                                        onFailure = { exception ->
                                            errorMessage = exception.message ?: "Failed to load dashboard"
                                            isLoading = false
                                        }
                                    )
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            summaryReport != null -> {
                DashboardContent(report = summaryReport!!)
            }
        }
    }
}

@Composable
private fun DashboardContent(report: DashboardSummaryResponse) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = report.period.replace("_", " ").split(" ").joinToString(" ") { 
                    it.replaceFirstChar { char -> char.uppercaseChar() }
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        item {
            // Quick Stats Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        title = "Total Operations",
                        value = report.totalOperations.toString(),
                        icon = Icons.Default.Settings,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    StatCard(
                        title = "Motor ON",
                        value = report.motorOnCount.toString(),
                        icon = Icons.Default.PlayArrow,
                        color = Color(0xFF4CAF50)
                    )
                }
                item {
                    StatCard(
                        title = "Motor OFF",
                        value = report.motorOffCount.toString(),
                        icon = Icons.Default.Close,
                        color = Color(0xFFF44336)
                    )
                }
                item {
                    StatCard(
                        title = "Status Requests",
                        value = report.statusRequests.toString(),
                        icon = Icons.Default.Info,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
        
        item {
            // Detailed Statistics
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Performance Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DashboardStatisticRow(
                        icon = Icons.Default.Phone,
                        label = "Unique Phone Numbers",
                        value = report.uniquePhoneNumbers.toString()
                    )
                    
                    report.averageVoltage?.let { voltage ->
                        DashboardStatisticRow(
                            icon = Icons.Default.Settings,
                            label = "Average Voltage",
                            value = "${voltage}V"
                        )
                    }
                    
                    report.averageCurrent?.let { current ->
                        DashboardStatisticRow(
                            icon = Icons.Default.PlayArrow,
                            label = "Average Current",
                            value = "${current}A"
                        )
                    }
                    
                    report.averageWaterLevel?.let { waterLevel ->
                        DashboardStatisticRow(
                            icon = Icons.Default.Info,
                            label = "Average Water Level",
                            value = "${waterLevel}%"
                        )
                    }
                }
            }
        }
        
        item {
            // System Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "System Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "System Online",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Last updated: ${java.util.Date()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val uptimePercentage = if (report.totalOperations > 0) {
                        (report.motorOnCount * 100 / report.totalOperations)
                    } else 0
                    
                    Text(
                        text = "Motor Uptime: ${uptimePercentage}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardStatisticRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}