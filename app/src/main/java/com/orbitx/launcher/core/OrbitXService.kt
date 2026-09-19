package com.orbitx.launcher.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.orbitx.launcher.utils.Logging

/**
 * OrbitXService - Background service for managing game processes and downloads
 */
class OrbitXService : Service() {
    
    companion object {
        private const val CHANNEL_ID = "OrbitXServiceChannel"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_NAME = "OrbitX Background Service"
        private const val CHANNEL_DESCRIPTION = "Background service for OrbitX Launcher"
    }
    
    private val binder = LocalBinder()
    private var isRunning = false
    
    inner class LocalBinder : Binder() {
        fun getService(): OrbitXService = this@OrbitXService
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        Logging.d("OrbitXService", "Service created")
        createNotificationChannel()
        startForeground()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logging.d("OrbitXService", "Service started")
        isRunning = true
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Logging.d("OrbitXService", "Service destroyed")
        isRunning = false
        stopForeground(true)
    }
    
    /**
     * Create notification channel for foreground service
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Start foreground service with notification
     */
    private fun startForeground() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    /**
     * Create notification for foreground service
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OrbitX Launcher")
            .setContentText("Background service running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
    
    /**
     * Check if service is running
     */
    fun isServiceRunning(): Boolean = isRunning
    
    /**
     * Stop the service
     */
    fun stopService() {
        stopSelf()
    }
}
