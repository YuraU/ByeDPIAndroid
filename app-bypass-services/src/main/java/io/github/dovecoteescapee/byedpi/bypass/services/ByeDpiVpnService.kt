package io.github.dovecoteescapee.byedpi.bypass.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import io.github.dovecoteescapee.byedpi.bypass.R
import io.github.dovecoteescapee.byedpi.bypass.data.ByPassManager
import io.github.dovecoteescapee.byedpi.bypass.data.ByeDpiProxyStatus
import io.github.dovecoteescapee.byedpi.bypass.di.AppByPassComponentHolder
import io.github.dovecoteescapee.byedpi.bypass.utility.createConnectionNotification
import io.github.dovecoteescapee.byedpi.bypass.utility.registerNotificationChannel
import io.github.dovecoteescapee.byedpi.common.storage.AppStatus
import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.setMode
import io.github.dovecoteescapee.byedpi.common.storage.setStatus
import io.github.dovecoteescapee.byedpi.common.storage.utils.getAppListType
import io.github.dovecoteescapee.byedpi.common.storage.utils.getSelectedApps
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ByeDpiVpnService : LifecycleVpnService() {

    private var isServiceStarted = false

    internal lateinit var byPassManager: ByPassManager
    internal lateinit var appSettings: KeyValueStorage<StorageType.AppSettings>

    override fun onCreate() {
        AppByPassComponentHolder.get().inject(this)
        super.onCreate()
        registerNotificationChannel(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.vpn_channel_name,
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
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
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

    override fun onRevoke() {
        Log.i(TAG, "VPN revoked")
        lifecycleScope.launch { stop() }
    }

    private fun start() {
        lifecycleScope.launch(Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Failed to start VPN")
            stop()
        }) {
            if (byPassManager.start()) {
                byPassManager.startTun2Socks(::createBuilder)
                startForeground()
                Log.i(TAG, "Vpn started")
                isServiceStarted = true
            } else {
                Log.e(TAG, "Failed to start VPN")
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
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(FOREGROUND_SERVICE_ID, notification)
        }
    }

    private fun stop() {
        Log.i(TAG, "Stopping")

        try {
            byPassManager.stopTun2Socks()
            byPassManager.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop VPN", e)
        } finally {
            stopSelf()
        }
    }

    private fun updateStatus(newStatus: ByeDpiProxyStatus) {
        Log.d(TAG, "App status changed to $newStatus")

        setStatus(
            when (newStatus) {
                ByeDpiProxyStatus.Connecting -> AppStatus.Starting
                ByeDpiProxyStatus.Connected -> AppStatus.Running
                ByeDpiProxyStatus.Disconnected,
                ByeDpiProxyStatus.Failed -> AppStatus.Halted
            }
        )
        setMode(Mode.VPN)

        if (newStatus == ByeDpiProxyStatus.Disconnected || newStatus == ByeDpiProxyStatus.Failed) {
            if (newStatus == ByeDpiProxyStatus.Failed) {
                Handler(Looper.getMainLooper()).post({
                    Toast.makeText(
                        this,
                        getString(R.string.failed_to_start, "vpn"),
                        Toast.LENGTH_SHORT,
                    ).show()
                })
            }

            if (isServiceStarted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                byPassManager.stopTun2Socks()
                stopSelf()
            }
        }
    }

    private fun createNotification(): Notification =
        createConnectionNotification(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_title,
            R.string.vpn_notification_content,
            ByeDpiVpnService::class.java,
        )

    private suspend fun createBuilder(dns: String, ipv6: Boolean): Builder {
        Log.d(TAG, "DNS: $dns")
        val builder = Builder()
        builder.setSession("ByeDPI")
        builder.setConfigureIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent().apply {
                    setClassName(
                        applicationContext,
                        "io.github.dovecoteescapee.byedpi.MainActivity"
                    )
                },
                PendingIntent.FLAG_IMMUTABLE,
            )
        )

        builder.addAddress("10.10.10.10", 32)
            .addRoute("0.0.0.0", 0)

        if (ipv6) {
            builder.addAddress("fd00::1", 128)
                .addRoute("::", 0)
        }

        if (dns.isNotBlank()) {
            builder.addDnsServer(dns)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        val listType = appSettings.getAppListType()
        val listedApps = appSettings.getSelectedApps()

        when (listType) {
            "blacklist" -> {
                for (packageName in listedApps) {
                    try {
                        builder.addDisallowedApplication(packageName)
                    } catch (e: Exception) {
                        Log.e(TAG, "Не удалось добавить приложение $packageName в черный список", e)
                    }
                }

                builder.addDisallowedApplication(applicationContext.packageName)
            }

            "whitelist" -> {
                for (packageName in listedApps) {
                    try {
                        builder.addAllowedApplication(packageName)
                    } catch (e: Exception) {
                        Log.e(TAG, "Не удалось добавить приложение $packageName в белый список", e)
                    }
                }
            }

            "disable" -> {
                builder.addDisallowedApplication(applicationContext.packageName)
            }
        }

        return builder
    }

    companion object {
        private val TAG: String = ByeDpiVpnService::class.java.simpleName
        private const val FOREGROUND_SERVICE_ID: Int = 7201
        private const val NOTIFICATION_CHANNEL_ID: String = "ByeDPIVpn"
    }
}
