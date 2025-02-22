package io.github.dovecoteescapee.byedpi.core

import io.github.dovecoteescapee.byedpi.bypass.feature.api.ByPassController
import io.github.dovecoteescapee.byedpi.bypass.feature.api.fromKeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.byeDpiProxyArgs
import io.github.dovecoteescapee.byedpi.common.storage.data.byedpi.ByeDpiProxyCmdPreferences
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import kotlinx.coroutines.runBlocking

object ByPassControllerImpl : ByPassController {

    private val byeDpiArgSettings
        get() = StorageComponentHolder.get().byeDpiArgSettings()

    private val dpiProxy by lazy {
        ByeDpiProxy()
    }

    override fun startProxy(cmd: String?): Int {
        val args = cmd?.let {
            ByeDpiProxyCmdPreferences.createFromCmd(cmd).args
        } ?: byeDpiProxyArgs?.args ?: runBlocking {
            fromKeyValueStorage(byeDpiArgSettings).args
        }

        return dpiProxy.startProxy(args)
    }

    override fun stopProxy(): Int {
        return dpiProxy.stopProxy()
    }

    override fun isProxyActive(): Boolean {
        return dpiProxy.isProxyActive()
    }

    override fun startTun2socks(configPath: String, fd: Int) {
        TProxyService.TProxyStartService(configPath, fd)
    }

    override fun stopTun2socks() {
        TProxyService.TProxyStopService()
    }
}
