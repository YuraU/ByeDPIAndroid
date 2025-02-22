package io.github.dovecoteescapee.byedpi.common.storage

import io.github.dovecoteescapee.byedpi.common.storage.data.byedpi.ByeDpiProxyArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val appStatusInternal = MutableStateFlow(AppStatus.Halted)
private val appModeInternal = MutableStateFlow(Mode.VPN)

val appStatus: StateFlow<AppStatus> = appStatusInternal.asStateFlow()

val appMode: StateFlow<Mode> = appModeInternal.asStateFlow()

var byeDpiProxyArgs: ByeDpiProxyArgs? = null

fun setStatus(status: AppStatus) {
    if (appStatusInternal.value != status) {
        appStatusInternal.tryEmit(status)
    }
}

fun setMode(mode: Mode) {
    appModeInternal.tryEmit(mode)
}

enum class AppStatus {
    Halted,
    Starting,
    Running,
}

enum class Mode {
    Proxy,
    VPN;

    companion object {

        fun fromString(name: String): Mode = when (name) {
            "proxy" -> Proxy
            "vpn" -> VPN
            else -> throw IllegalArgumentException("Invalid mode: $name")
        }
    }
}
