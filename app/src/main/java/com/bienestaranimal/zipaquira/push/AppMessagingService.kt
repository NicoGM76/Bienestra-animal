package com.bienestaranimal.zipaquira.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        PushTokenStore.save(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Bienestar Animal Zipaquirá"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Tienes una nueva actualización."

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Actualizaciones de casos",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "case_updates"
    }
}
