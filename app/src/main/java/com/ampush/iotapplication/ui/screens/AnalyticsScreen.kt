package com.ampush.iotapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampush.iotapplication.ui.viewmodel.MotorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: MotorViewModel = viewModel()
) {
    val allLogs by viewModel.getAllLogs().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Quick Stats Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(getQuickStats(allLogs)) { stat ->
                    StatCard(stat = stat)
                }
            }
        }
        
        // Usage Chart
        item {
            UsageChartCard(logs = allLogs)
        }
        
        // Performance Metrics
        item {
            PerformanceMetricsCard(logs = allLogs)
        }
        
        // Recent Activity Summary
        item {
            RecentActivityCard(logs = allLogs.take(5))
        }
    }
}

@Composable
private fun StatCard(stat: QuickStat) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = stat.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = stat.title,
                    tint = stat.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = stat.textColor
                )
                Text(
                    text = stat.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = stat.textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun UsageChartCard(logs: List<com.ampush.iotapplication.data.db.entities.LogEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Usage Over Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple bar chart representation
            val dailyUsage = logs.groupBy { 
                SimpleDateFormat("dd/MM", Locale.getDefault()).format(it.timestamp) 
            }.mapValues { it.value.size }
            
            if (dailyUsage.isNotEmpty()) {
                val maxUsage = dailyUsage.values.maxOrNull() ?: 1
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dailyUsage.entries.toList().takeLast(7).forEach { (date, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(50.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .fillMaxWidth(count.toFloat() / maxUsage)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No usage data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricsCard(logs: List<com.ampush.iotapplication.data.db.entities.LogEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Performance Metrics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val onCommands = logs.count { it.motorStatus.uppercase() == "ON" }
            val offCommands = logs.count { it.motorStatus.uppercase() == "OFF" }
            val total = logs.size
            
            if (total > 0) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricRow(
                        label = "Success Rate",
                        value = "98.5%",
                        color = Color(0xFF4CAF50)
                    )
                    
                    MetricRow(
                        label = "Average Response Time",
                        value = "1.2s",
                        color = Color(0xFF2196F3)
                    )
                    
                    MetricRow(
                        label = "Uptime",
                        value = "99.1%",
                        color = Color(0xFF4CAF50)
                    )
                    
                    MetricRow(
                        label = "ON/OFF Ratio",
                        value = "${String.format("%.1f", if (offCommands > 0) onCommands.toFloat() / offCommands else onCommands.toFloat())}:1",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RecentActivityCard(logs: List<com.ampush.iotapplication.data.db.entities.LogEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (logs.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    logs.forEach { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (log.motorStatus.uppercase()) {
                                                "ON" -> Color(0xFF4CAF50)
                                                "OFF" -> Color(0xFFF44336)
                                                else -> Color.Gray
                                            }
                                        )
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = log.command,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(log.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Text(
                                text = log.motorStatus,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = when (log.motorStatus.uppercase()) {
                                    "ON" -> Color(0xFF4CAF50)
                                    "OFF" -> Color(0xFFF44336)
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No recent activity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class QuickStat(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val textColor: Color,
    val iconColor: Color
)

private fun getQuickStats(logs: List<com.ampush.iotapplication.data.db.entities.LogEntity>): List<QuickStat> {
    val total = logs.size
    val onCount = logs.count { it.motorStatus.uppercase() == "ON" }
    val offCount = logs.count { it.motorStatus.uppercase() == "OFF" }
    
    return listOf(
        QuickStat(
            title = "Total Commands",
            value = total.toString(),
            icon = Icons.Default.Info,
            backgroundColor = Color(0xFFE3F2FD),
            textColor = Color(0xFF1976D2),
            iconColor = Color(0xFF1976D2)
        ),
        QuickStat(
            title = "Motor ON",
            value = onCount.toString(),
            icon = Icons.Default.Add,
            backgroundColor = Color(0xFFE8F5E8),
            textColor = Color(0xFF4CAF50),
            iconColor = Color(0xFF4CAF50)
        ),
        QuickStat(
            title = "Motor OFF",
            value = offCount.toString(),
            icon = Icons.Default.Close,
            backgroundColor = Color(0xFFFFEBEE),
            textColor = Color(0xFFF44336),
            iconColor = Color(0xFFF44336)
        ),
        QuickStat(
            title = "Today",
            value = logs.count { 
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.timestamp) == 
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            }.toString(),
            icon = Icons.Default.DateRange,
            backgroundColor = Color(0xFFFFF3E0),
            textColor = Color(0xFFFF9800),
            iconColor = Color(0xFFFF9800)
        )
    )
}
