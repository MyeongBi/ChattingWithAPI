package com.example.chattingwithapi

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date


class MyFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("MyFcmService", "New token :: $token")
        sendTokenToServer(token)
    }

    fun sendTokenToServer(token: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.98:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.sendToken(token)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("MyFcmService", "Token sent to server successfully")
                    // 성공적으로 토큰을 서버에 전송한 경우에 수행할 작업을 추가합니다.
                } else {
                    Log.e("MyFcmService", "Failed to send token to server. Error: ${response.code()}")
                    // 토큰 전송에 실패한 경우에 대한 예외 처리를 수행합니다.
                }

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("MyFcmService", "Failed to send token to server. Error: ${t.message}")
                // 토큰 전송에 실패한 경우에 대한 예외 처리를 수행합니다.
            }
        })
    }



    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            showNotification(it)
        }
    }

    private fun showNotification(notification: RemoteMessage.Notification) {
        val intent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = getString(R.string.my_notification_channel_id)

        val notificationManager = NotificationManagerCompat.from(this)

        val channel = NotificationChannel(channelId, "알림", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setContentIntent(pIntent)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(Date().time.toInt(), notificationBuilder.build())
    }
}
