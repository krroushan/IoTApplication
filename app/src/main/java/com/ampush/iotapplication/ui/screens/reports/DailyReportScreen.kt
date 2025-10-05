package com.ampush.iotapplication.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ampush.iotapplication.network.RetrofitClient
import com.ampush.iotapplication.network.models.ReportResponse
import com.ampush.iotapplication.repository.ReportRepository
import com.ampush.iotapplication.utils.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import java.util.Date

@Composable
fun DailyReportScreen(
    selectedDate: Date = Date()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val apiService = remember { RetrofitClient.apiService }
    val reportRepository = remember { ReportRepository(apiService, sessionManager) }
    val coroutineScope = rememberCoroutineScope()
    
    var dailyReport by remember { mutableStateOf<ReportResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(selectedDate) {
        isLoading = true
        errorMessage = null
        
        val result = reportRepository.getDailyReport(selectedDate)
        result.fold(
            onSuccess = { report ->
                dailyReport = report
                isLoading = false
            },
            onFailure = { exception ->
                errorMessage = exception.message ?: "Failed to load daily report"
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
                imageVector = Icons.Default.DateRange,
                contentDescription = "Calendar",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Daily Report",
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
                        Text("Loading daily report...")
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
                            text = "Error Loading Report",
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
                                    val result = reportRepository.getDailyReport(selectedDate)
                                    result.fold(
                                        onSuccess = { report ->
                                            dailyReport = report
                                            isLoading = false
                                        },
                                        onFailure = { exception ->
                                            errorMessage = exception.message ?: "Failed to load daily report"
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
            
            dailyReport != null -> {
                DailyReportContent(report = dailyReport!!)
            }
        }
    }
}

@Composable
private fun DailyReportContent(report: ReportResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Statistics
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                StatisticRow(
                    icon = Icons.Default.Settings,
                    label = "Total Operations",
                    value = report.totalOperations.toString()
                )
                
                StatisticRow(
                    icon = Icons.Default.PlayArrow,
                    label = "Motor ON Count"


,
                    value = report.motorOnCount.toString()
                )
                
                StatisticRow(
                    icon = Icons.Default.Close,
                    label = "Motor OFF Count",
                    value = report.motorOffCount.toString()
                )
                
                StatisticRow(
                    icon = Icons.Default.Info,
                    label = "Status Requests",
                    value = report.statusRequests.toString()
                )
            }
        }
        
        // Electrical Measurements
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Electrical Measurements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                report.averageVoltage?.let { voltage ->
                    StatisticRow(
                        icon = Icons.Default.Settings,
                        label = "Average Voltage",
                        value = "${voltage}V"
                    )
                }
                
                report.averageCurrent?.let { current ->
                    StatisticRow(
                        icon = Icons.Default.PlayArrow,
                        label = "Average Current",
                        value = "${current}A"
                    )
                }
                
                report.averageWaterLevel?.let { waterLevel ->
                    StatisticRow(
                        icon = Icons.Default.Info,
                        label = "Average Water Level",
                        value = "${waterLevel}%"
                    )
                }
            }
        }
        
        // Time Analysis
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Time Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                StatisticRow(
                    icon = Icons.Default.Info,
                    label = "Total Minutes",
                    value = "${report.totalMinutes}"
                )
                
                StatisticRow(
                    icon = Icons.Default.PlayArrow,
                    label = "Uptime",
                    value = report.uptime,
                    valueColor = Color(0xFF4CAF50)
                )
                
                StatisticRow(
                    icon = Icons.Default.Close,
                    label = "Downtime",
                    value = report.downtime,
                    valueColor = Color(0xFFF44336)
                )
                
                StatisticRow(
                    icon = Icons.Default.Info,
                    label = "Uptime Percentage",
                    value = "${(report.uptimeMinutes * 100 / report.totalMinutes)}%",
                    valueColor = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun StatisticRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
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
            fontWeight = FontWeight.Medium,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
    }
}
