package com.ampush.iotapplication.data.model

import java.util.Date

data class MotorData(
    val motorStatus: String,
    val voltage: Float?,
    val current: Float?,
    val waterLevel: Float?,
    val mode: String?,
    val clock: String?,
    val runTime: Int?,
    val rawMessage: String,
    val phoneNumber: String,
    val timestamp: Date = Date()
)
