package com.ampush.iotapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ampush.iotapplication.network.models.MotorLogEntity

@Composable
fun DeviceStatsSummary(logs: List<MotorLogEntity>) {
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
                text = "Device Statistics (API)",
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
fun DeviceHistoryItem(log: MotorLogEntity) {
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
            
            // API timestamp
            Text(
                text = log.timestamp,
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
