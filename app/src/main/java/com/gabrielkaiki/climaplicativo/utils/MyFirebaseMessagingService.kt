package com.gabrielkaiki.climaplicativo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gabrielkaiki.climaplicativo.R
import com.gabrielkaiki.climaplicativo.activity.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        var titulo = message.notification!!.title
        var notification = message.notification!!.body

        enviarNotificacao(titulo, notification)
    }

    private fun enviarNotificacao(titulo: String?, notification: String?) {

        notificacao = true
        var canal = getString(R.string.default_notification_channel_id)
        var uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var intent = Intent(this, SplashActivity::class.java)
        intent.putExtra("notificacao", true)

        var notificacaoBuilder = NotificationCompat.Builder(this, canal)
            .setContentTitle(titulo)
            .setContentText(notification)
            .setSmallIcon(R.drawable.logo_mini)
            .setSound(uriSom)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            var pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            notificacaoBuilder.setContentIntent(pendingIntent)
        } else {
            var pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            notificacaoBuilder.setContentIntent(pendingIntent)
        }


        var notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel =
                NotificationChannel(canal, "canal", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificacaoBuilder.build())

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("info", "onNewToken")
    }
}