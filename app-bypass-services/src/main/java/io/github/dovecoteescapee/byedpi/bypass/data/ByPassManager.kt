package io.github.dovecoteescapee.byedpi.bypass.data

import android.content.Context
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.WorkerThread
import io.github.dovecoteescapee.byedpi.bypass.feature.api.ByPassController
import io.github.dovecoteescapee.byedpi.bypass.feature.api.utility.isProxyAvailable
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.getDns
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProxyIpAndPort
import io.github.dovecoteescapee.byedpi.common.storage.utils.isIpV6Enabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class ByPassManager(
    private val ctx: Context,
    private val byPassController: ByPassController,
    private val appSettings: KeyValueStorage<StorageType.AppSettings>,
    private val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>,
) {

    private var tunFd: ParcelFileDescriptor? = null

    private var _status: MutableStateFlow<ByeDpiProxyStatus> = MutableStateFlow(ByeDpiProxyStatus.Disconnected)

    private var stopping: Boolean = false

    private val proxyExecutorService = Executors.newSingleThreadExecutor()
    private val activeJobExecutorService = Executors.newSingleThreadExecutor()

    private val callable: Callable<Int> = Callable<Int> {
        val code = try {
            byPassController.startProxy()
        } catch (e: Exception) {
            -1
        }

        code
    }

    val status: Flow<ByeDpiProxyStatus> = _status.asStateFlow()

    suspend fun start(): Boolean {
        if (_status.value == ByeDpiProxyStatus.Connected) {
            Log.w(TAG, "Proxy already connected")
            return true
        }

        var wasError = false

        activeJobExecutorService.submit {
            Log.i(TAG, "Starting proxy")
            val code = startProxy()
            wasError = true
            Log.i(TAG, "Proxy stopped with code $code")
            handleProxyCode(code)
        }

        val (ip, port) = byeDpiSettings.getProxyIpAndPort()

        Log.i(TAG, "waiting for proxy start")
        val status = waitingForProxyStart(ip, port)

        if (status && !wasError) {
            Log.w(TAG, "Proxy connected")
            _status.value = ByeDpiProxyStatus.Connected
        } else {
            Log.w(TAG, "Proxy failed")
            _status.value = ByeDpiProxyStatus.Failed
        }

        return status
    }

    fun stop() {
        if (_status.value == ByeDpiProxyStatus.Disconnected) {
            Log.w(TAG, "Proxy already disconnected")
            return
        }

        Log.w(TAG, "Stopping proxy")

        stopping = true
        try {
            byPassController.stopProxy()
            _status.value = ByeDpiProxyStatus.Disconnected
            Log.w(TAG, "Proxy stopped")
        } catch (e: Exception) {
            Log.w(TAG, "Proxy stopped failed")
            _status.value = ByeDpiProxyStatus.Failed
            throw e
        } finally {
            stopping = false
        }
    }

    fun shutdown() {
        stop()
        proxyExecutorService.shutdown()
        activeJobExecutorService.shutdown()
    }

    suspend fun startTun2Socks(createBuilder: suspend (dns: String, ipv6: Boolean) -> VpnService.Builder) {
        Log.i(TAG, "Starting tun2socks")

        if (tunFd != null) {
            _status.value = ByeDpiProxyStatus.Failed
            throw IllegalStateException("VPN field not null")
        }

        val (ip, port) = byeDpiSettings.getProxyIpAndPort()

        val dns = appSettings.getDns()
        val ipv6 = appSettings.isIpV6Enabled()

        val tun2socksConfig = """
        | misc:
        |   task-stack-size: 81920
        | socks5:
        |   mtu: 8500
        |   address: $ip
        |   port: $port
        |   udp: udp
        """.trimMargin("| ")

        val configPath = try {
            File.createTempFile("config", "tmp", ctx.cacheDir).apply {
                writeText(tun2socksConfig)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create config file", e)
            throw e
        }

        val fd = createBuilder(dns, ipv6).establish()
            ?: throw IllegalStateException("VPN connection failed")

        this.tunFd = fd

        byPassController.startTun2socks(configPath.absolutePath, fd.fd)

        Log.i(TAG, "Tun2Socks started. ip: $ip port: $port")
    }

    fun stopTun2Socks() {
        if (tunFd != null) {
            Log.i(TAG, "Stopping tun2socks")

            byPassController.stopTun2socks()

            try {
                File(ctx.cacheDir, "config.tmp").delete()
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to delete config file", e)
            }

            tunFd?.close() ?: Log.w(TAG, "VPN not running")
            tunFd = null

            Log.i(TAG, "Tun2socks stopped")
        }
    }

    private fun handleProxyCode(code: Int) {
        if (stopping) {
            Log.i(TAG, "Proxy stopped by user")
        } else if (code != 0) {
            Log.i(TAG, "Proxy stopped with error $code")
            _status.value = ByeDpiProxyStatus.Failed
            stop()
        } else {
            Log.i(TAG, "Proxy stopped correctly with code 0")
            _status.value = ByeDpiProxyStatus.Disconnected
        }
    }

    @WorkerThread
    private fun startProxy(): Int {
        return proxyExecutorService.submit(callable).get()
    }

    private suspend fun waitingForProxyStart(
        ip: String,
        port: String,
        timeoutMillis: Long = 5_000L
    ): Boolean {
        val startTime = System.currentTimeMillis()
        var tryCount = 0
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            delay(100L)
            if (byPassController.isProxyActive() || (tryCount > 12 && isProxyAvailable(ip, port.toInt()))) {
                return true
            } else if (_status.value == ByeDpiProxyStatus.Failed || stopping){
                return false
            }
            tryCount++

        }
        return false
    }

    companion object {
        private val TAG: String = ByPassManager::class.java.simpleName
    }
}

enum class ByeDpiProxyStatus {
    Connecting,
    Connected,
    Disconnected,
    Failed,
}
