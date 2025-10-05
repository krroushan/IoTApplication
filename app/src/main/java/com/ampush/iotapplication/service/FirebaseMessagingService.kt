package com.ampush.iotapplication.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ampush.iotapplication.MainActivity
import com.ampush.iotapplication.R
import com.ampush.iotapplication.utils.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Logger.d("FCM message received", "FCM_SERVICE")
        Logger.d("From: ${remoteMessage.from}", "FCM_SERVICE")
        Logger.d("Message ID: ${remoteMessage.messageId}", "FCM_SERVICE")
        
        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Logger.d("Message data payload: ${remoteMessage.data}", "FCM_SERVICE")
        }
        
        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Logger.d("Message notification body: ${notification.body}", "FCM_SERVICE")
            showNotification(notification.title, notification.body)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.i("FCM token refreshed: ${token.take(20)}...", "FCM_SERVICE")
        
        // Send token to server
        val fcmTokenManager = com.ampush.iotapplication.data.manager.FcmTokenManager(this)
        fcmTokenManager.refreshFcmToken()
    }
    
    private fun showNotification(title: String?, body: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val channelId = "motor_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "Motor Control")
            .setContentText(body ?: "New notification")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Motor Control Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for motor control system"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(0, notificationBuilder.build())
    }
}
