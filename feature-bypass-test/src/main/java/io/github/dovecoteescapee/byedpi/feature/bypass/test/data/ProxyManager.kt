package io.github.dovecoteescapee.byedpi.feature.bypass.test.data

import android.util.Log
import io.github.dovecoteescapee.byedpi.bypass.feature.api.ByPassController
import io.github.dovecoteescapee.byedpi.bypass.feature.api.utility.isProxyAvailable
import kotlinx.coroutines.delay

class ProxyManager(
    private val byPassController: ByPassController,
) {

    private val proxyIp: String = "127.0.0.1"
    private val proxyPort: Int = 10080

    fun startProxyService(cmd: String): Int {
        val testCmd = "--ip $proxyIp --port $proxyPort $cmd"

        try {
            return byPassController.startProxy(testCmd)
        } catch (e: Exception) {
            Log.e(TAG, "Error start proxy service: ${e.message}")
            throw e
        }
    }

   fun stopProxyService(): Int {
        return try {
            byPassController.stopProxy()
        } catch (e: Exception) {
            Log.e(TAG, "Error stop proxy service: ${e.message}")
            throw e
        }
    }

    suspend fun waitForProxyStatus(
        timeoutMillis: Long = 5_000L
    ): Boolean {
        var tryCount = 0
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            delay(100)
            if (byPassController.isProxyActive() || (tryCount > 12 && isProxyAvailable(proxyIp, proxyPort))) {
                return true
            }

            tryCount++
        }
        return false
    }

    private companion object {
        const val TAG = "ProxyManagerTest"
    }
}
