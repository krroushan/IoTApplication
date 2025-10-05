package com.ampush.iotapplication.data.db.dao

import androidx.room.*
import com.ampush.iotapplication.data.db.entities.LogEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface LogDao {
    
    @Query("SELECT * FROM motor_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntity>>
    
    @Query("SELECT * FROM motor_logs WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getLogsByDateRange(startDate: Date, endDate: Date): Flow<List<LogEntity>>
    
    @Query("SELECT * FROM motor_logs WHERE timestamp >= :startDate ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLog(startDate: Date): LogEntity?
    
    @Query("SELECT * FROM motor_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<LogEntity>
    
    @Query("SELECT * FROM motor_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLog(): LogEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<LogEntity>)
    
    @Update
    suspend fun updateLog(log: LogEntity)
    
    @Query("UPDATE motor_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
    
    @Query("UPDATE motor_logs SET command = 'STATUS' WHERE command = 'STATUS_RESPONSE'")
    suspend fun fixStatusResponseCommand(): Int
    
    @Query("UPDATE motor_logs SET motorStatus = 'STATUS' WHERE motorStatus = 'UNKNOWN'")
    suspend fun fixUnknownMotorStatus(): Int
    
    @Delete
    suspend fun deleteLog(log: LogEntity)
    
    @Query("DELETE FROM motor_logs WHERE timestamp < :cutoffDate")
    suspend fun deleteOldLogs(cutoffDate: Date)
}
