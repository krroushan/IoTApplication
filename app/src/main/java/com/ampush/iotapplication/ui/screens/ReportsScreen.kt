package com.ampush.iotapplication.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampush.iotapplication.network.models.*
import com.ampush.iotapplication.ui.viewmodel.ReportsViewModel
import com.ampush.iotapplication.data.manager.DeviceManager
import com.ampush.iotapplication.utils.SessionManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel()
) {
    val context = LocalContext.current
    val deviceManager = remember { DeviceManager(context) }
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Devices state - refresh from API on screen load
    var savedDevices by remember { mutableStateOf(deviceManager.getSavedDevices()) }
    var isRefreshingDevices by remember { mutableStateOf(false) }
    
    // Refresh devices from API when screen loads
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val token = sessionManager.getAccessToken()
                if (token != null) {
                    isRefreshingDevices = true
                    val refreshSuccess = deviceManager.refreshDevices(token)
                    if (refreshSuccess) {
                        savedDevices = deviceManager.getSavedDevices()
                        com.ampush.iotapplication.utils.Logger.i("Devices refreshed in Reports: ${savedDevices.size} devices", "REPORTS")
                    }
                } else {
                    // Use cached devices if no token
                    savedDevices = deviceManager.getSavedDevices()
                }
            } catch (e: Exception) {
                com.ampush.iotapplication.utils.Logger.e("Error refreshing devices in Reports", e, "REPORTS")
                // Use cached devices on error
                savedDevices = deviceManager.getSavedDevices()
            } finally {
                isRefreshingDevices = false
            }
        }
    }
    
    // Device selector state
    var expandedDeviceMenu by remember { mutableStateOf(false) }
    var selectedDeviceId by remember { mutableStateOf<Int?>(null) } // null = All Devices
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Day", "Month", "Year", "Custom")
    
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Update ViewModel when device selection changes
    LaunchedEffect(selectedDeviceId) {
        viewModel.setDeviceFilter(selectedDeviceId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { 
                        selectedTab = index
                        when (index) {
                            0 -> viewModel.loadDailyReport()
                            1 -> viewModel.loadMonthlyReport()
                            2 -> viewModel.loadYearlyReport()
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        
        // Device Selector
        if (savedDevices.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Device:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Box {
                        TextButton(
                            onClick = { expandedDeviceMenu = true }
                        ) {
                            Text(
                                text = if (selectedDeviceId == null) "All Devices" else savedDevices.find { it.id == selectedDeviceId }?.deviceName ?: "All Devices",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF31b84f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Device",
                                tint = Color(0xFF31b84f)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = expandedDeviceMenu,
                            onDismissRequest = { expandedDeviceMenu = false }
                        ) {
                            // All Devices option
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "All Devices",
                                        fontWeight = if (selectedDeviceId == null) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedDeviceId = null
                                    expandedDeviceMenu = false
                                }
                            )
                            
                            Divider()
                            
                            // Device options
                            savedDevices.forEach { device ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = device.deviceName,
                                            fontWeight = if (selectedDeviceId == device.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedDeviceId = device.id
                                        expandedDeviceMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Error Message
        if (errorMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> DailyReportContent(viewModel)
                1 -> MonthlyReportContent(viewModel)
                2 -> YearlyReportContent(viewModel)
                3 -> CustomReportContent(viewModel)
            }
            
            // Loading Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ==================== Daily Report Content ====================

@Composable
fun DailyReportContent(viewModel: ReportsViewModel) {
    val dailyReport by viewModel.dailyReport.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selector
        item {
            DateNavigationCard(
                date = dateFormat.format(selectedDate),
                onPreviousClick = { viewModel.navigateToPreviousDay() },
                onNextClick = { viewModel.navigateToNextDay() }
            )
        }
        
        // Daily Consumption Summary
        item {
            if (dailyReport != null) {
                DailyConsumptionCard(dailyReport!!.summary)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data available for selected date",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Hourly Chart
        item {
            if (dailyReport != null && dailyReport!!.hourlyData.isNotEmpty()) {
                HourlyChartCard(dailyReport!!.hourlyData)
            }
        }
        
        // Device-wise Breakdown
        item {
            if (dailyReport?.deviceWiseBreakdown != null && dailyReport!!.deviceWiseBreakdown!!.isNotEmpty()) {
                DeviceBreakdownCard(dailyReport!!.deviceWiseBreakdown!!)
            }
        }
        
        // Flexible Statistics Section
        item {
            FlexibleStatisticsSection()
        }
    }
}

@Composable
fun DateNavigationCard(
    date: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = date,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DailyConsumptionCard(summary: DailyReportSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Energy Consumption
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Energy",
                        tint = Color(0xFF31b84f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Energy Consumption:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "${String.format("%.2f", summary.dailyConsumption)} kWh",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF31b84f)
                )
            }
            
            Divider()
            
            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Electricity Cost
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Electricity Bill",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%.2f", summary.totalCost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5c345a)
                    )
                }
                
                // Water Pumped
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Water Pumped",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalWater} L",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Runtime
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total Runtime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalRuntime} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Motor Cycles
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Motor Cycles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.motorCycles}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyChartCard(hourlyData: List<HourlyData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hourly Consumption",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple Area Chart
            HourlyAreaChart(hourlyData)
        }
    }
}

@Composable
fun HourlyAreaChart(hourlyData: List<HourlyData>) {
    val maxEnergy = hourlyData.maxOfOrNull { it.energy } ?: 1f
    val chartColor = Color(0xFF2196F3)
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (hourlyData.size - 1)
        
        // Draw area
        val path = Path()
        hourlyData.forEachIndexed { index, data ->
            val x = index * spacing
            val y = height - (data.energy / maxEnergy * height)
            
            if (index == 0) {
                path.moveTo(x, height)
                path.lineTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.lineTo(width, height)
        path.close()
        
        drawPath(
            path = path,
            color = chartColor.copy(alpha = 0.3f)
        )
        
        // Draw line
        val linePath = Path()
        hourlyData.forEachIndexed { index, data ->
            val x = index * spacing
            val y = height - (data.energy / maxEnergy * height)
            
            if (index == 0) {
                linePath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
            }
        }
        
        drawPath(
            path = linePath,
            color = chartColor,
            style = Stroke(width = 4f)
        )
        
        // Draw time labels
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 24f
            color = android.graphics.Color.GRAY
        }
        
        listOf(0, 6, 12, 18, 23).forEach { hour ->
            val index = hourlyData.indexOfFirst { it.hour.startsWith(String.format("%02d", hour)) }
            if (index >= 0) {
                val x = index * spacing
                drawContext.canvas.nativeCanvas.drawText(
                    hourlyData[index].hour,
                    x,
                    height + 40f,
                    paint
                )
            }
        }
    }
}

// ==================== Monthly Report Content ====================

@Composable
fun MonthlyReportContent(viewModel: ReportsViewModel) {
    val monthlyReport by viewModel.monthlyReport.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedMonthYear.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector
        item {
            MonthNavigationCard(
                month = selectedMonth,
                year = selectedYear,
                onPreviousClick = { viewModel.navigateToPreviousMonth() },
                onNextClick = { viewModel.navigateToNextMonth() }
            )
        }
        
        // Monthly Summary
        item {
            if (monthlyReport != null) {
                MonthlyProductionCard(monthlyReport!!.summary)
            }
        }
        
        // Daily Bar Chart
        item {
            if (monthlyReport != null && monthlyReport!!.dailyData.isNotEmpty()) {
                DailyBarChartCard(monthlyReport!!.dailyData)
            }
        }
        
        // Device Breakdown
        item {
            if (monthlyReport?.deviceWiseBreakdown != null && monthlyReport!!.deviceWiseBreakdown!!.isNotEmpty()) {
                DeviceBreakdownCard(monthlyReport!!.deviceWiseBreakdown!!)
            }
        }
        
        // Flexible Statistics
        item {
            FlexibleStatisticsSection()
        }
    }
}

@Composable
fun MonthNavigationCard(
    month: Int,
    year: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).apply {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        format(cal.time)
    }.format(Calendar.getInstance().apply { set(Calendar.MONTH, month - 1) }.time)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = "$monthName $year",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MonthlyProductionCard(summary: MonthlyReportSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Energy Consumption
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Energy",
                        tint = Color(0xFF31b84f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Monthly Consumption:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "${String.format("%.2f", summary.monthlyConsumption)} kWh",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF31b84f)
                )
            }
            
            Divider()
            
            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Electricity Cost
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Electricity Bill",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%.2f", summary.totalCost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5c345a)
                    )
                }
                
                // Water Pumped
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Water Pumped",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalWater} L",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Runtime
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total Runtime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalRuntime} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Motor Cycles
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Motor Cycles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.motorCycles}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DailyBarChartCard(dailyData: List<DailyData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Daily Consumption",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bar Chart
            DailyBarChart(dailyData)
        }
    }
}

@Composable
fun DailyBarChart(dailyData: List<DailyData>) {
    val maxEnergy = dailyData.maxOfOrNull { it.energy } ?: 1f
    val barColor = Color(0xFF2196F3)
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / (dailyData.size + 2)
        val barSpacing = 2f
        
        dailyData.forEachIndexed { index, data ->
            val barHeight = (data.energy / maxEnergy * height)
            val x = (index + 1) * barWidth
            
            drawRect(
                color = barColor,
                topLeft = Offset(x + barSpacing, height - barHeight),
                size = Size(barWidth - barSpacing * 2, barHeight)
            )
        }
        
        // Draw day labels (only for specific days)
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 20f
            color = android.graphics.Color.GRAY
        }
        
        listOf(1, 10, 20, dailyData.size).forEach { day ->
            if (day <= dailyData.size) {
                val index = day - 1
                val x = (index + 1) * barWidth + barWidth / 2
                drawContext.canvas.nativeCanvas.drawText(
                    day.toString(),
                    x,
                    height + 35f,
                    paint
                )
            }
        }
    }
}

// ==================== Yearly Report Content ====================

@Composable
fun YearlyReportContent(viewModel: ReportsViewModel) {
    val yearlyReport by viewModel.yearlyReport.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Year Selector
        item {
            YearNavigationCard(
                year = selectedYear,
                onPreviousClick = { viewModel.navigateToPreviousYear() },
                onNextClick = { viewModel.navigateToNextYear() }
            )
        }
        
        // Annual Summary
        item {
            if (yearlyReport != null) {
                AnnualProductionCard(yearlyReport!!.summary)
            }
        }
        
        // Monthly Bar Chart
        item {
            if (yearlyReport != null && yearlyReport!!.monthlyData.isNotEmpty()) {
                MonthlyBarChartCard(yearlyReport!!.monthlyData)
            }
        }
        
        // Device Breakdown
        item {
            if (yearlyReport?.deviceWiseBreakdown != null && yearlyReport!!.deviceWiseBreakdown!!.isNotEmpty()) {
                DeviceBreakdownCard(yearlyReport!!.deviceWiseBreakdown!!)
            }
        }
        
        // Flexible Statistics
        item {
            FlexibleStatisticsSection()
        }
    }
}

@Composable
fun YearNavigationCard(
    year: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AnnualProductionCard(summary: YearlyReportSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Energy Consumption
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Energy",
                        tint = Color(0xFF31b84f),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Annual Consumption:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = if (summary.annualConsumption >= 1000) "${summary.annualConsumption/1000} MWh" else "${summary.annualConsumption} kWh",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF31b84f)
                )
            }
            
            Divider()
            
            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Electricity Cost
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Electricity Bill",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%.2f", summary.totalCost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5c345a)
                    )
                }
                
                // Water Pumped
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Water Pumped",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalWater} L",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Runtime
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total Runtime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalRuntime} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Motor Cycles
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Motor Cycles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.motorCycles}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyBarChartCard(monthlyData: List<MonthlyData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Monthly Consumption",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bar Chart
            MonthlyBarChart(monthlyData)
        }
    }
}

@Composable
fun MonthlyBarChart(monthlyData: List<MonthlyData>) {
    val maxEnergy = monthlyData.maxOfOrNull { it.energy } ?: 1f
    val barColor = Color(0xFF2196F3)
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / 13
        val barSpacing = 2f
        
        monthlyData.forEachIndexed { index, data ->
            val barHeight = (data.energy / maxEnergy * height)
            val x = index * barWidth
            
            drawRect(
                color = if (data.energy > 0) barColor else Color.Gray.copy(alpha = 0.3f),
                topLeft = Offset(x + barSpacing, height - barHeight),
                size = Size(barWidth - barSpacing * 2, barHeight.coerceAtLeast(1f))
            )
        }
        
        // Draw month labels
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 18f
            color = android.graphics.Color.GRAY
        }
        
        monthlyData.forEachIndexed { index, data ->
            val x = index * barWidth + barWidth / 2
            drawContext.canvas.nativeCanvas.drawText(
                data.month.toString(),
                x,
                height + 30f,
                paint
            )
        }
    }
}

// ==================== Custom Range Report Content ====================

@Composable
fun CustomReportContent(viewModel: ReportsViewModel) {
    val customReport by viewModel.customReport.collectAsState()
    
    // Date Range State
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Range Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select Date Range",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Start Date
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Start Date"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (startDate != null) "Start: ${dateFormat.format(startDate)}" else "Select Start Date"
                        )
                    }
                    
                    // End Date
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = startDate != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "End Date"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (endDate != null) "End: ${dateFormat.format(endDate)}" else "Select End Date"
                        )
                    }
                    
                    // Generate Report Button
                    Button(
                        onClick = {
                            if (startDate != null && endDate != null) {
                                viewModel.loadCustomReport(
                                    startDate = startDate!!,
                                    endDate = endDate!!
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = startDate != null && endDate != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF31b84f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Generate"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Report")
                    }
                }
            }
        }
        
        // Custom Report Summary
        item {
            if (customReport != null) {
                CustomReportSummaryCard(customReport!!.summary, customReport!!.dateRange)
            }
        }
        
        // Custom Chart
        item {
            if (customReport != null && customReport!!.data.isNotEmpty()) {
                CustomRangeChartCard(customReport!!.data)
            }
        }
        
        // Device Breakdown
        item {
            if (customReport?.deviceWiseBreakdown != null && customReport!!.deviceWiseBreakdown!!.isNotEmpty()) {
                DeviceBreakdownCard(customReport!!.deviceWiseBreakdown!!)
            }
        }
        
        // Flexible Statistics
        item {
            FlexibleStatisticsSection()
        }
    }
    
    // Simple Date Picker Dialog (Start Date)
    if (showStartDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                startDate = calendar.time
                // Reset end date if it's before start date
                if (endDate != null && endDate!!.before(startDate)) {
                    endDate = null
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    // Simple Date Picker Dialog (End Date)
    if (showEndDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                endDate = calendar.time
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            minDate = startDate
        )
    }
}

@Composable
fun SimpleDatePickerDialog(
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit,
    minDate: Date? = null
) {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(Unit) {
        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                onDateSelected(year, month, day)
            },
            currentYear,
            currentMonth,
            currentDay
        )
        
        // Set min date if provided
        if (minDate != null) {
            datePickerDialog.datePicker.minDate = minDate.time
        }
        
        // Set max date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        
        datePickerDialog.setOnDismissListener {
            onDismiss()
        }
        
        datePickerDialog.show()
    }
}

@Composable
fun CustomReportSummaryCard(summary: CustomReportSummary, dateRange: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = dateRange,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Energy",
                    tint = Color(0xFF31b84f),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Total Consumption:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${String.format("%.2f", summary.totalConsumption)} kWh",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF31b84f)
                )
            }
            
            Divider()
            
            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Electricity Cost
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Electricity Bill",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%.2f", summary.totalCost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5c345a)
                    )
                }
                
                // Water Pumped
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Water Pumped",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalWater} L",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Runtime
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total Runtime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalRuntime} min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Motor Cycles
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Motor Cycles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.motorCycles}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Total Days
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total Days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalDays.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CustomRangeChartCard(data: List<DailyData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Consumption Over Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bar Chart
            CustomRangeBarChart(data)
        }
    }
}

@Composable
fun CustomRangeBarChart(data: List<DailyData>) {
    val maxEnergy = data.maxOfOrNull { it.energy } ?: 1f
    val barColor = Color(0xFF2196F3)
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = if (data.size > 0) width / (data.size + 2) else 20f
        val barSpacing = 2f
        
        data.forEachIndexed { index, dayData ->
            val barHeight = (dayData.energy / maxEnergy * height)
            val x = (index + 1) * barWidth
            
            drawRect(
                color = if (dayData.energy > 0) barColor else Color.Gray.copy(alpha = 0.3f),
                topLeft = Offset(x + barSpacing, height - barHeight),
                size = Size(barWidth - barSpacing * 2, barHeight.coerceAtLeast(1f))
            )
        }
        
        // Draw labels for first, middle, and last days
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 18f
            color = android.graphics.Color.GRAY
        }
        
        if (data.isNotEmpty()) {
            val firstDay = data.first().day
            val lastDay = data.last().day
            val middleIndex = data.size / 2
            val middleDay = if (middleIndex < data.size) data[middleIndex].day else 0
            
            // First day
            val x1 = barWidth + barWidth / 2
            drawContext.canvas.nativeCanvas.drawText(
                firstDay.toString(),
                x1,
                height + 30f,
                paint
            )
            
            // Middle day
            if (data.size > 2) {
                val x2 = (middleIndex + 1) * barWidth + barWidth / 2
                drawContext.canvas.nativeCanvas.drawText(
                    middleDay.toString(),
                    x2,
                    height + 30f,
                    paint
                )
            }
            
            // Last day
            val x3 = data.size * barWidth + barWidth / 2
            drawContext.canvas.nativeCanvas.drawText(
                lastDay.toString(),
                x3,
                height + 30f,
                paint
            )
        }
    }
}

// ==================== Common Components ====================

@Composable
fun DeviceBreakdownCard(devices: List<DeviceWiseBreakdown>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Device Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            devices.forEach { device ->
                DeviceBreakdownItem(device)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DeviceBreakdownItem(device: DeviceWiseBreakdown) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = device.deviceName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Energy and Cost Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Energy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format("%.2f", device.energy)} kWh",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF31b84f)
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Cost",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${String.format("%.2f", device.cost)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5c345a)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Water, Runtime, Cycles Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Water",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${device.water} L",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2196F3)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Runtime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${device.runtime} min",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Cycles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${device.cycles}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FlexibleStatisticsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Info",
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Flexible Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize the time period to flexibly match monthly electricity bill data!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

