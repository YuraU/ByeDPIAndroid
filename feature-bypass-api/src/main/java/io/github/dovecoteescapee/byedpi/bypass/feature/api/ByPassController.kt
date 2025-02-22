package io.github.dovecoteescapee.byedpi.bypass.feature.api

import androidx.annotation.WorkerThread

interface ByPassController {

    @WorkerThread
    fun startProxy(cmd: String? = null): Int
    fun stopProxy(): Int
    fun isProxyActive(): Boolean

    fun startTun2socks(configPath: String, fd: Int)
    fun stopTun2socks()
}