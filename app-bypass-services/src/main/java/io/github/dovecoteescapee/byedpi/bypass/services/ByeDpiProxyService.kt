package io.github.dovecoteescapee.byedpi.bypass.services

import android.app.Notification
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.github.dovecoteescapee.byedpi.bypass.R
import io.github.dovecoteescapee.byedpi.bypass.data.ByPassManager
import io.github.dovecoteescapee.byedpi.bypass.data.ByeDpiProxyStatus
import io.github.dovecoteescapee.byedpi.bypass.di.AppByPassComponentHolder
import io.github.dovecoteescapee.byedpi.bypass.utility.createConnectionNotification
import io.github.dovecoteescapee.byedpi.bypass.utility.registerNotificationChannel
import io.github.dovecoteescapee.byedpi.common.storage.AppStatus
import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.setMode
import io.github.dovecoteescapee.byedpi.common.storage.setStatus
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ByeDpiProxyService : LifecycleService() {

    private var isServiceStarted = false

    internal lateinit var byPassManager: ByPassManager

    override fun onCreate() {
        AppByPassComponentHolder.get().inject(this)
        super.onCreate()
        registerNotificationChannel(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.proxy_channel_name,
        )

        lifecycleScope.launch {
            byPassManager.status
                .onEach {
                    updateStatus(it)
                }
                .collect()
        }
    }

    override fun onDestroy() {
        byPassManager.shutdown()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            stopForeground(STOP_FOREGROUND_REMOVE) // Android 14+
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true) // Android 13 и ниже
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return when (val action = intent?.action) {
            START_ACTION -> {
                start()
                START_STICKY
            }

            STOP_ACTION -> {
                stop()
                START_NOT_STICKY
            }

            else -> {
                Log.w(TAG, "Unknown action: $action")
                START_NOT_STICKY
            }
        }
    }

    private fun start() {
        lifecycleScope.launch(Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Failed to start Proxy")
            stop()
        }) {
            Log.i(TAG, "Starting")

            if (byPassManager.start()) {
                startForeground()
                Log.i(TAG, "Proxy service started")
                isServiceStarted = true
            } else {
                Log.e(TAG, "Failed to start Proxy")
                stop()
            }
        }
    }

    private fun startForeground() {
        val notification: Notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_SERVICE_ID,
                notification,
                FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(FOREGROUND_SERVICE_ID, notification)
        }
    }

    private fun stop() {
        Log.i(TAG, "Stopping")

        try {
            byPassManager.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop PROXY", e)
        } finally {
            stopSelf()
        }

        stopSelf()
    }

    private fun updateStatus(newStatus: ByeDpiProxyStatus) {
        Log.d(TAG, "Proxy status changed to $newStatus")

        setStatus(
            when (newStatus) {
                ByeDpiProxyStatus.Connecting -> AppStatus.Starting
                ByeDpiProxyStatus.Connected -> AppStatus.Running
                ByeDpiProxyStatus.Disconnected,
                ByeDpiProxyStatus.Failed -> AppStatus.Halted
            }
        )

        setMode(Mode.Proxy)

        if (newStatus == ByeDpiProxyStatus.Disconnected || newStatus == ByeDpiProxyStatus.Failed) {
            if (newStatus == ByeDpiProxyStatus.Failed) {
                Handler(Looper.getMainLooper()).post({
                    Toast.makeText(
                        this,
                        getString(R.string.failed_to_start, "proxy"),
                        Toast.LENGTH_SHORT,
                    ).show()
                })
            }

            if (isServiceStarted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    stopForeground(STOP_FOREGROUND_REMOVE) // Android 14+
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true) // Android 13 и ниже
                }
                stopSelf()
            }
        }
    }

    private fun createNotification(): Notification =
        createConnectionNotification(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_title,
            R.string.proxy_notification_content,
            ByeDpiProxyService::class.java,
        )

    companion object {
        private val TAG: String = ByeDpiProxyService::class.java.simpleName
        private const val FOREGROUND_SERVICE_ID: Int = 7202
        private const val NOTIFICATION_CHANNEL_ID: String = "ByeDPI Proxy"
    }
}
