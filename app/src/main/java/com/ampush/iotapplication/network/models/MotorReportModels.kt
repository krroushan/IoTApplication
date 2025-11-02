package com.ampush.iotapplication.network.models

import com.google.gson.annotations.SerializedName

// ==================== Common Models ====================

// Note: DeviceInfo and UserInfo are already defined in MotorLogModels.kt
// We'll use those existing models instead of redefining them

data class CustomerInfo(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    
    @SerializedName("unitPrice")
    val unitPrice: Float,
    
    @SerializedName("pumpingCapacity")
    val pumpingCapacity: Int
)

data class DeviceWiseBreakdown(
    @SerializedName("deviceId")
    val deviceId: Int,
    
    @SerializedName("deviceName")
    val deviceName: String,
    
    @SerializedName("smsNumber")
    val smsNumber: String,
    
    @SerializedName("energy")
    val energy: Float,
    
    @SerializedName("runtime")
    val runtime: Int,
    
    @SerializedName("water")
    val water: Int,
    
    @SerializedName("cost")
    val cost: Float,
    
    @SerializedName("cycles")
    val cycles: Int
)

// ==================== Daily Report Models ====================

data class HourlyData(
    @SerializedName("hour")
    val hour: String,
    
    @SerializedName("energy")
    val energy: Float,
    
    @SerializedName("power")
    val power: Float,
    
    @SerializedName("runtime")
    val runtime: Int,
    
    @SerializedName("water")
    val water: Int,
    
    @SerializedName("cost")
    val cost: Float,
    
    @SerializedName("cycles")
    val cycles: Int
)

data class DailyReportSummary(
    @SerializedName("currentPower")
    val currentPower: Int,
    
    @SerializedName("dailyConsumption")
    val dailyConsumption: Float,
    
    @SerializedName("totalRuntime")
    val totalRuntime: Int,
    
    @SerializedName("totalWater")
    val totalWater: Int,
    
    @SerializedName("totalCost")
    val totalCost: Float,
    
    @SerializedName("motorCycles")
    val motorCycles: Int,
    
    @SerializedName("averageRuntime")
    val averageRuntime: Float,
    
    @SerializedName("unitPrice")
    val unitPrice: Float,
    
    @SerializedName("pumpingCapacity")
    val pumpingCapacity: Int
)

data class DailyReportResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("reportType")
    val reportType: String,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("summary")
    val summary: DailyReportSummary,
    
    @SerializedName("hourlyData")
    val hourlyData: List<HourlyData>,
    
    @SerializedName("deviceWiseBreakdown")
    val deviceWiseBreakdown: List<DeviceWiseBreakdown>? = null,
    
    @SerializedName("device")
    val device: DeviceInfo? = null,
    
    @SerializedName("customer")
    val customer: CustomerInfo? = null
)

// ==================== Monthly Report Models ====================

data class DailyData(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("day")
    val day: Int,
    
    @SerializedName("energy")
    val energy: Float,
    
    @SerializedName("runtime")
    val runtime: Int,
    
    @SerializedName("water")
    val water: Int,
    
    @SerializedName("cost")
    val cost: Float,
    
    @SerializedName("cycles")
    val cycles: Int
)

data class MonthlyReportSummary(
    @SerializedName("monthlyConsumption")
    val monthlyConsumption: Float,
    
    @SerializedName("totalRuntime")
    val totalRuntime: Int,
    
    @SerializedName("totalWater")
    val totalWater: Int,
    
    @SerializedName("totalCost")
    val totalCost: Float,
    
    @SerializedName("motorCycles")
    val motorCycles: Int,
    
    @SerializedName("averageDailyConsumption")
    val averageDailyConsumption: Float,
    
    @SerializedName("averageDailyRuntime")
    val averageDailyRuntime: Int,
    
    @SerializedName("unitPrice")
    val unitPrice: Float,
    
    @SerializedName("pumpingCapacity")
    val pumpingCapacity: Int
)

data class MonthlyReportResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("reportType")
    val reportType: String,
    
    @SerializedName("month")
    val month: Int,
    
    @SerializedName("year")
    val year: Int,
    
    @SerializedName("monthName")
    val monthName: String,
    
    @SerializedName("summary")
    val summary: MonthlyReportSummary,
    
    @SerializedName("dailyData")
    val dailyData: List<DailyData>,
    
    @SerializedName("deviceWiseBreakdown")
    val deviceWiseBreakdown: List<DeviceWiseBreakdown>? = null,
    
    @SerializedName("device")
    val device: DeviceInfo? = null,
    
    @SerializedName("customer")
    val customer: CustomerInfo? = null
)

// ==================== Yearly Report Models ====================

data class MonthlyData(
    @SerializedName("month")
    val month: Int,
    
    @SerializedName("monthName")
    val monthName: String,
    
    @SerializedName("energy")
    val energy: Float,
    
    @SerializedName("runtime")
    val runtime: Int,
    
    @SerializedName("water")
    val water: Int,
    
    @SerializedName("cost")
    val cost: Float,
    
    @SerializedName("cycles")
    val cycles: Int
)

data class YearlyReportSummary(
    @SerializedName("annualConsumption")
    val annualConsumption: Float,
    
    @SerializedName("totalRuntime")
    val totalRuntime: Int,
    
    @SerializedName("totalWater")
    val totalWater: Int,
    
    @SerializedName("totalCost")
    val totalCost: Float,
    
    @SerializedName("motorCycles")
    val motorCycles: Int,
    
    @SerializedName("averageMonthlyConsumption")
    val averageMonthlyConsumption: Float,
    
    @SerializedName("averageMonthlyRuntime")
    val averageMonthlyRuntime: Int,
    
    @SerializedName("unitPrice")
    val unitPrice: Float,
    
    @SerializedName("pumpingCapacity")
    val pumpingCapacity: Int
)

data class YearlyReportResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("reportType")
    val reportType: String,
    
    @SerializedName("year")
    val year: Int,
    
    @SerializedName("summary")
    val summary: YearlyReportSummary,
    
    @SerializedName("monthlyData")
    val monthlyData: List<MonthlyData>,
    
    @SerializedName("deviceWiseBreakdown")
    val deviceWiseBreakdown: List<DeviceWiseBreakdown>? = null,
    
    @SerializedName("device")
    val device: DeviceInfo? = null,
    
    @SerializedName("customer")
    val customer: CustomerInfo? = null
)

// ==================== Custom Range Report Models ====================

data class CustomReportSummary(
    @SerializedName("totalConsumption")
    val totalConsumption: Float,
    
    @SerializedName("totalRuntime")
    val totalRuntime: Int,
    
    @SerializedName("totalWater")
    val totalWater: Int,
    
    @SerializedName("totalCost")
    val totalCost: Float,
    
    @SerializedName("motorCycles")
    val motorCycles: Int,
    
    @SerializedName("totalDays")
    val totalDays: Int,
    
    @SerializedName("averageDailyConsumption")
    val averageDailyConsumption: Float,
    
    @SerializedName("averageDailyRuntime")
    val averageDailyRuntime: Int,
    
    @SerializedName("unitPrice")
    val unitPrice: Float,
    
    @SerializedName("pumpingCapacity")
    val pumpingCapacity: Int
)

data class CustomReportResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("reportType")
    val reportType: String,
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String,
    
    @SerializedName("dateRange")
    val dateRange: String,
    
    @SerializedName("groupBy")
    val groupBy: String,
    
    @SerializedName("summary")
    val summary: CustomReportSummary,
    
    @SerializedName("data")
    val data: List<DailyData>,
    
    @SerializedName("deviceWiseBreakdown")
    val deviceWiseBreakdown: List<DeviceWiseBreakdown>? = null,
    
    @SerializedName("device")
    val device: DeviceInfo? = null,
    
    @SerializedName("customer")
    val customer: CustomerInfo? = null
)

