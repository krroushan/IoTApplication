package com.ampush.iotapplication.ui.components

import androidx.compose.foundation.clickable
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
import com.ampush.iotapplication.data.manager.DefaultDeviceManager
import com.ampush.iotapplication.data.model.Device

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultDeviceSelectionDialog(
    devices: List<Device>,
    currentDefaultDevice: Device?,
    onDeviceSelected: (Device?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val defaultDeviceManager = remember { DefaultDeviceManager(context) }
    var selectedDevice by remember { mutableStateOf<Device?>(currentDefaultDevice) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Default Device",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // No Default Device option
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDevice = null },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedDevice == null) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "No default",
                                tint = if (selectedDevice == null) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "No Default Device",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedDevice == null) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Always show device selection dialog",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedDevice == null) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            RadioButton(
                                selected = selectedDevice == null,
                                onClick = { selectedDevice = null },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
                
                // Device options
                items(devices) { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDevice = device },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedDevice == device) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Device",
                                tint = if (selectedDevice == device) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.deviceName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedDevice == device) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = device.smsNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedDevice == device) 
                                        MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                device.description?.let { description ->
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectedDevice == device) 
                                            MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            RadioButton(
                                selected = selectedDevice == device,
                                onClick = { selectedDevice = device },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    defaultDeviceManager.setDefaultDevice(selectedDevice)
                    onDeviceSelected(selectedDevice)
                    onDismiss()
                }
            ) {
                Text("Set Default")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
