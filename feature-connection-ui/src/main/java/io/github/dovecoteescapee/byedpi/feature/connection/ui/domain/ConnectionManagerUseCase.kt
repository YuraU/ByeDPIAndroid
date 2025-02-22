package io.github.dovecoteescapee.byedpi.feature.connection.ui.domain

import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.feature.connection.ui.data.ByDpiServiceManager

class ConnectionManagerUseCase(
    private val serviceManager: ByDpiServiceManager
) {

    fun start(mode: Mode) {
        when (mode) {
            Mode.Proxy -> serviceManager.startProxy()
            Mode.VPN -> serviceManager.startVpn()
        }
    }

    fun stop(mode: Mode) {
        when (mode) {
            Mode.Proxy -> serviceManager.stopProxy()
            Mode.VPN -> serviceManager.stopVpn()
        }
    }
}
