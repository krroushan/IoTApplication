package com.ampush.iotapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ampush.iotapplication.data.db.entities.LogEntity
import com.ampush.iotapplication.data.repository.MotorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import com.ampush.iotapplication.utils.Logger

class MotorViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = MotorRepository(application.applicationContext)
    
    // UI State
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _latestLog = MutableStateFlow<LogEntity?>(null)
    val latestLog: StateFlow<LogEntity?> = _latestLog
    
    // Commands
    private val _commandResult = MutableLiveData<Boolean>()
    val commandResult: LiveData<Boolean> = _commandResult
    
    init {
        Logger.d("MotorViewModel initialized")
        // Start periodic sync
        repository.schedulePeriodicSync()
        
        // Load latest log
        loadLatestLog()
    }
    
    // SMS Commands
    fun sendMotorOn() {
        Logger.logMotorCommand("MOTOR_ON", "INITIATED")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.sendMotorOn()
                _commandResult.value = result
                if (result) {
                    Logger.logMotorCommand("MOTOR_ON", "SUCCESS")
                    loadLatestLog()
                } else {
                    Logger.logMotorCommand("MOTOR_ON", "FAILED")
                }
            } catch (e: Exception) {
                Logger.logMotorError("MOTOR_ON", e.message ?: "Unknown error", e)
                _errorMessage.value = "Failed to send Motor ON command: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendMotorOff() {
        Logger.logMotorCommand("MOTOR_OFF", "INITIATED")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.sendMotorOff()
                _commandResult.value = result
                if (result) {
                    Logger.logMotorCommand("MOTOR_OFF", "SUCCESS")
                    loadLatestLog()
                } else {
                    Logger.logMotorCommand("MOTOR_OFF", "FAILED")
                }
            } catch (e: Exception) {
                Logger.logMotorError("MOTOR_OFF", e.message ?: "Unknown error", e)
                _errorMessage.value = "Failed to send Motor OFF command: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendStatusRequest() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.sendStatusRequest()
                _commandResult.value = result
                if (result) {
                    loadLatestLog()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send Status request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Data Access
    fun getAllLogs(): Flow<List<LogEntity>> = repository.getAllLogs()
    
    fun getLogsByDateRange(startDate: Date, endDate: Date): Flow<List<LogEntity>> = 
        repository.getLogsByDateRange(startDate, endDate)
    
    private fun loadLatestLog() {
        viewModelScope.launch {
            try {
                val log = repository.getLatestLog()
                _latestLog.value = log
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load latest log: ${e.message}"
            }
        }
    }
    
    // Cleanup
    fun cleanupOldLogs() {
        viewModelScope.launch {
            try {
                repository.cleanupOldLogs()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to cleanup old logs: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
