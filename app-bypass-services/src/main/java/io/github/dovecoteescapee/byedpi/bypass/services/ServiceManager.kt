package io.github.dovecoteescapee.byedpi.bypass.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

object ServiceManager {

    private val TAG: String = ServiceManager::class.java.simpleName

    fun startVpn(context: Context) {
        Log.i(TAG, "Starting VPN")
        val intent = Intent(context, ByeDpiVpnService::class.java)
        intent.action = START_ACTION
        // ContextCompat.startForegroundService(context, intent)
        context.startService(intent)
        /* try {
            context.startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vpn service")
            e.printStackTrace()
        } */
    }

    fun stopVpn(context: Context) {
        Log.i(TAG, "Stopping VPN")
        val intent = Intent(context, ByeDpiVpnService::class.java)
        intent.action = STOP_ACTION
        ContextCompat.startForegroundService(context, intent)
    }

    fun startProxy(context: Context) {
        Log.i(TAG, "Starting proxy")
        val intent = Intent(context, ByeDpiProxyService::class.java)
        intent.action = START_ACTION
        // ContextCompat.startForegroundService(context, intent)
        context.startService(intent)
    }

    fun stopProxy(context: Context) {
        Log.i(TAG, "Stopping proxy")
        val intent = Intent(context, ByeDpiProxyService::class.java)
        intent.action = STOP_ACTION
        ContextCompat.startForegroundService(context, intent)
    }
}
