package io.github.dovecoteescapee.byedpi.feature.connection.ui.data

import android.content.Context
import io.github.dovecoteescapee.byedpi.bypass.services.ServiceManager

class ByDpiServiceManager(
    private val context: Context
) {

    fun startVpn() {
        ServiceManager.startVpn(context)
    }

    fun stopVpn() {
        ServiceManager.stopVpn(context)
    }

    fun startProxy() {
        ServiceManager.startProxy(context)
    }

    fun stopProxy() {
        ServiceManager.stopProxy(context)
    }
}
