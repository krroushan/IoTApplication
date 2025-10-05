package com.ampush.iotapplication.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "motor_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Date,
    val motorStatus: String, // ON, OFF, STATUS
    val voltage: Float?,
    val current: Float?,
    val waterLevel: Float?,
    val mode: String?,
    val clock: String?,
    val runTime: Int?,
    val command: String, // MOTORON, MOTOROFF, STATUS
    val phoneNumber: String,
    val isSynced: Boolean = false
)
