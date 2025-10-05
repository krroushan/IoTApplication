package com.ampush.iotapplication.network.models

import com.google.gson.annotations.SerializedName

data class LogResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("motorStatus")
    val motorStatus: String,
    @SerializedName("voltage")
    val voltage: Float?,
    @SerializedName("current")
    val current: Float?,
    @SerializedName("waterLevel")
    val waterLevel: Float?,
    @SerializedName("mode")
    val mode: String?,
    @SerializedName("clock")
    val clock: String?,
    @SerializedName("command")
    val command: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("synced")
    val synced: Boolean
)

data class LogsResponse(
    @SerializedName("logs")
    val logs: List<LogResponse>,
    @SerializedName("total")
    val total: Int
)
