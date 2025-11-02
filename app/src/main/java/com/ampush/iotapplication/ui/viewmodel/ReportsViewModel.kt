package com.ampush.iotapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ampush.iotapplication.network.ApiService
import com.ampush.iotapplication.network.RetrofitClient
import com.ampush.iotapplication.network.models.*
import com.ampush.iotapplication.repository.MotorReportsRepository
import com.ampush.iotapplication.utils.Logger
import com.ampush.iotapplication.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sessionManager = SessionManager(application)
    private val apiService: ApiService = RetrofitClient.apiService
    private val repository = MotorReportsRepository(apiService, sessionManager)
    
    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Daily Report State
    private val _dailyReport = MutableStateFlow<DailyReportResponse?>(null)
    val dailyReport: StateFlow<DailyReportResponse?> = _dailyReport.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()
    
    // Monthly Report State
    private val _monthlyReport = MutableStateFlow<MonthlyReportResponse?>(null)
    val monthlyReport: StateFlow<MonthlyReportResponse?> = _monthlyReport.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()
    
    private val _selectedMonthYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedMonthYear: StateFlow<Int> = _selectedMonthYear.asStateFlow()
    
    // Yearly Report State
    private val _yearlyReport = MutableStateFlow<YearlyReportResponse?>(null)
    val yearlyReport: StateFlow<YearlyReportResponse?> = _yearlyReport.asStateFlow()
    
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()
    
    // Custom Report State
    private val _customReport = MutableStateFlow<CustomReportResponse?>(null)
    val customReport: StateFlow<CustomReportResponse?> = _customReport.asStateFlow()
    
    // Selected Device Filter
    private val _selectedDeviceId = MutableStateFlow<Int?>(null)
    val selectedDeviceId: StateFlow<Int?> = _selectedDeviceId.asStateFlow()
    
    init {
        Logger.d("ReportsViewModel initialized", "REPORTS_VM")
        // Load today's report by default
        loadDailyReport()
    }
    
    // ==================== Daily Report ====================
    
    fun loadDailyReport(date: Date? = null, deviceId: Int? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val targetDate = date ?: _selectedDate.value
                val targetDeviceId = deviceId ?: _selectedDeviceId.value
                
                Logger.d("Loading daily report for: $targetDate, device: $targetDeviceId", "REPORTS_VM")
                
                val result = repository.getDailyReport(
                    date = targetDate,
                    deviceId = targetDeviceId
                )
                
                result.onSuccess { report ->
                    _dailyReport.value = report
                    _selectedDate.value = targetDate
                    Logger.i("Daily report loaded successfully", "REPORTS_VM")
                }.onFailure { error ->
                    val message = "Failed to load daily report: ${error.message}"
                    _errorMessage.value = message
                    Logger.e(message, error, "REPORTS_VM")
                }
            } catch (e: Exception) {
                val message = "Error loading daily report: ${e.message}"
                _errorMessage.value = message
                Logger.e(message, e, "REPORTS_VM")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectDate(date: Date) {
        _selectedDate.value = date
        loadDailyReport(date)
    }
    
    fun navigateToPreviousDay() {
        val calendar = Calendar.getInstance()
        calendar.time = _selectedDate.value
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        selectDate(calendar.time)
    }
    
    fun navigateToNextDay() {
        val calendar = Calendar.getInstance()
        calendar.time = _selectedDate.value
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        
        // Don't go beyond today
        if (calendar.time <= Date()) {
            selectDate(calendar.time)
        }
    }
    
    // ==================== Monthly Report ====================
    
    fun loadMonthlyReport(month: Int? = null, year: Int? = null, deviceId: Int? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val targetMonth = month ?: _selectedMonth.value
                val targetYear = year ?: _selectedMonthYear.value
                val targetDeviceId = deviceId ?: _selectedDeviceId.value
                
                Logger.d("Loading monthly report for: $targetMonth/$targetYear, device: $targetDeviceId", "REPORTS_VM")
                
                val result = repository.getMonthlyReport(
                    month = targetMonth,
                    year = targetYear,
                    deviceId = targetDeviceId
                )
                
                result.onSuccess { report ->
                    _monthlyReport.value = report
                    _selectedMonth.value = targetMonth
                    _selectedMonthYear.value = targetYear
                    Logger.i("Monthly report loaded successfully", "REPORTS_VM")
                }.onFailure { error ->
                    val message = "Failed to load monthly report: ${error.message}"
                    _errorMessage.value = message
                    Logger.e(message, error, "REPORTS_VM")
                }
            } catch (e: Exception) {
                val message = "Error loading monthly report: ${e.message}"
                _errorMessage.value = message
                Logger.e(message, e, "REPORTS_VM")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedMonthYear.value = year
        loadMonthlyReport(month, year)
    }
    
    fun navigateToPreviousMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, _selectedMonth.value - 1)
        calendar.set(Calendar.YEAR, _selectedMonthYear.value)
        calendar.add(Calendar.MONTH, -1)
        
        selectMonth(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }
    
    fun navigateToNextMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, _selectedMonth.value - 1)
        calendar.set(Calendar.YEAR, _selectedMonthYear.value)
        calendar.add(Calendar.MONTH, 1)
        
        // Don't go beyond current month
        val now = Calendar.getInstance()
        if (calendar.get(Calendar.YEAR) < now.get(Calendar.YEAR) ||
            (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) && 
             calendar.get(Calendar.MONTH) <= now.get(Calendar.MONTH))) {
            selectMonth(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
        }
    }
    
    // ==================== Yearly Report ====================
    
    fun loadYearlyReport(year: Int? = null, deviceId: Int? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val targetYear = year ?: _selectedYear.value
                val targetDeviceId = deviceId ?: _selectedDeviceId.value
                
                Logger.d("Loading yearly report for: $targetYear, device: $targetDeviceId", "REPORTS_VM")
                
                val result = repository.getYearlyReport(
                    year = targetYear,
                    deviceId = targetDeviceId
                )
                
                result.onSuccess { report ->
                    _yearlyReport.value = report
                    _selectedYear.value = targetYear
                    Logger.i("Yearly report loaded successfully", "REPORTS_VM")
                }.onFailure { error ->
                    val message = "Failed to load yearly report: ${error.message}"
                    _errorMessage.value = message
                    Logger.e(message, error, "REPORTS_VM")
                }
            } catch (e: Exception) {
                val message = "Error loading yearly report: ${e.message}"
                _errorMessage.value = message
                Logger.e(message, e, "REPORTS_VM")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectYear(year: Int) {
        _selectedYear.value = year
        loadYearlyReport(year)
    }
    
    fun navigateToPreviousYear() {
        selectYear(_selectedYear.value - 1)
    }
    
    fun navigateToNextYear() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val nextYear = _selectedYear.value + 1
        
        // Don't go beyond current year
        if (nextYear <= currentYear) {
            selectYear(nextYear)
        }
    }
    
    // ==================== Custom Report ====================
    
    fun loadCustomReport(
        startDate: Date,
        endDate: Date,
        deviceId: Int? = null,
        groupBy: String = "day"
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val targetDeviceId = deviceId ?: _selectedDeviceId.value
                
                Logger.d("Loading custom report: $startDate to $endDate, device: $targetDeviceId", "REPORTS_VM")
                
                val result = repository.getCustomReport(
                    startDate = startDate,
                    endDate = endDate,
                    deviceId = targetDeviceId,
                    groupBy = groupBy
                )
                
                result.onSuccess { report ->
                    _customReport.value = report
                    Logger.i("Custom report loaded successfully", "REPORTS_VM")
                }.onFailure { error ->
                    val message = "Failed to load custom report: ${error.message}"
                    _errorMessage.value = message
                    Logger.e(message, error, "REPORTS_VM")
                }
            } catch (e: Exception) {
                val message = "Error loading custom report: ${e.message}"
                _errorMessage.value = message
                Logger.e(message, e, "REPORTS_VM")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // ==================== Device Filter ====================
    
    fun setDeviceFilter(deviceId: Int?) {
        _selectedDeviceId.value = deviceId
        
        // Reload current reports with new filter
        loadDailyReport(deviceId = deviceId)
        loadMonthlyReport(deviceId = deviceId)
        loadYearlyReport(deviceId = deviceId)
    }
    
    fun clearDeviceFilter() {
        setDeviceFilter(null)
    }
    
    // ==================== Error Handling ====================
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    // ==================== Refresh ====================
    
    fun refreshCurrentReport(reportType: String) {
        when (reportType) {
            "daily" -> loadDailyReport()
            "monthly" -> loadMonthlyReport()
            "yearly" -> loadYearlyReport()
        }
    }
    
    fun refreshAllReports() {
        loadDailyReport()
        loadMonthlyReport()
        loadYearlyReport()
    }
}

