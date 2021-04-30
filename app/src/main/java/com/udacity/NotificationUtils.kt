package com.udacity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import timber.log.Timber


// Notification ID.
private val NOTIFICATION_ID = 0

fun NotificationManager.sendNotification(
    status: Boolean,
    fileName: String,
    applicationContext: Context
) {

    // Create the content intent for the notification, which launches
    // this activity
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)

    // provide the status from the download and the filename to the detailAcitivty
    val bundle = Bundle().apply {
        putBoolean("status", status)
        putString("fileName", fileName)
    }
    contentIntent.putExtras(bundle)

    // Create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Set the notification content
    var builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        .setSmallIcon(R.drawable.baseline_cloud_download_24)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(applicationContext.getString(R.string.notification_description))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        // Add check the status button
        .addAction(
            R.drawable.baseline_cloud_download_24,
            applicationContext.getString(R.string.notification_button),
            contentPendingIntent
        )

    // Call notify
    notify(NOTIFICATION_ID, builder.build())
}

/**
 * Cancels all notifications.
 *
 */
fun NotificationManager.cancelNotifications() {
    Timber.i("cancelNotifications")
    cancelAll()
}