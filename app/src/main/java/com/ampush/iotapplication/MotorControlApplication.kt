package com.ampush.iotapplication

import android.app.Application
import com.ampush.iotapplication.data.db.AppDatabase
import com.ampush.iotapplication.data.repository.MotorRepository
import com.ampush.iotapplication.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MotorControlApplication : Application() {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any global components here
        // Database is initialized lazily when first accessed
        
        // Fix any existing logs with invalid command/motorStatus values
        applicationScope.launch {
            try {
                val repository = MotorRepository(applicationContext)
                repository.fixInvalidLogs()
            } catch (e: Exception) {
                Logger.e("Failed to fix invalid logs on startup", e, "APP_STARTUP")
            }
        }
    }
}
