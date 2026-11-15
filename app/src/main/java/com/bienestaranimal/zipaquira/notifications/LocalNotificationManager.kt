package com.bienestaranimal.zipaquira.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bienestaranimal.zipaquira.MainActivity

/**
 * Plan B académico: convierte notificaciones internas de Firestore en
 * notificaciones locales de Android sin depender de Cloud Functions/Blaze.
 *
 * No es push remoto: se ejecuta cuando la app consulta la bandeja de avisos.
 */
class LocalNotificationManager(context: Context) {

    private val appContext = context.applicationContext
    private val preferences =
        appContext.getSharedPreferences("local_notification_state", Context.MODE_PRIVATE)

    fun showUnread(notifications: List<NotificationSummary>) {
        notifications
            .filter { !it.read && !wasShown(it.id) }
            .forEach { item ->
                show(item)
                markShown(item.id)
            }
    }

    private fun show(item: NotificationSummary) {
        val manager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(appContext, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(appContext)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(item.title.ifBlank { "Bienestar Animal Zipaquirá" })
            .setContentText(item.body.ifBlank { "Tienes una nueva actualización." })
            .setStyle(
                Notification.BigTextStyle()
                    .bigText(item.body.ifBlank { "Tienes una nueva actualización." })
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(item.id.hashCode(), notification)
    }

    private fun wasShown(id: String): Boolean =
        preferences.getBoolean("shown_$id", false)

    private fun markShown(id: String) {
        preferences.edit().putBoolean("shown_$id", true).apply()
    }

    companion object {
        const val CHANNEL_ID = "case_updates"
    }
}
