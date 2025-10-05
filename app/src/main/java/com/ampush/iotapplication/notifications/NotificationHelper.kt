package com.ampush.iotapplication.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ampush.iotapplication.MainActivity
import com.ampush.iotapplication.R
import com.ampush.iotapplication.data.model.MotorData

class NotificationHelper(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "motor_control_channel"
        private const val CHANNEL_NAME = "Motor Control Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for motor status changes and SMS updates"
        
        private const val MOTOR_STATUS_NOTIFICATION_ID = 1001
        private const val SMS_NOTIFICATION_ID = 1002
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showMotorStatusNotification(motorData: MotorData) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = "Motor Status Update"
        val content = buildMotorStatusContent(motorData)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(MOTOR_STATUS_NOTIFICATION_ID, notification)
    }
    
    fun showSmsReceivedNotification(sender: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SMS Received")
            .setContentText("From: $sender")
            .setStyle(NotificationCompat.BigTextStyle().bigText("From: $sender\nMessage: $message"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(SMS_NOTIFICATION_ID, notification)
    }
    
    fun showSyncNotification(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Data Sync")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    private fun buildMotorStatusContent(motorData: MotorData): String {
        val builder = StringBuilder()
        builder.append("Motor: ${motorData.motorStatus}")
        
        motorData.voltage?.let { builder.append("\nVoltage: ${it}V") }
        motorData.current?.let { builder.append("\nCurrent: ${it}A") }
        motorData.waterLevel?.let { builder.append("\nWater Level: ${it}%") }
        motorData.mode?.let { builder.append("\nMode: ${it}") }
        motorData.clock?.let { builder.append("\nTime: ${it}") }
        
        return builder.toString()
    }
}
