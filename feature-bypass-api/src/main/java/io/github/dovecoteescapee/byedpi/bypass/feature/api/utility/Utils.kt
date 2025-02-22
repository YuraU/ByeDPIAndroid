package io.github.dovecoteescapee.byedpi.bypass.feature.api.utility

import java.net.InetSocketAddress
import java.net.Socket

fun isProxyAvailable(host: String, port: Int, timeout: Int = 200): Boolean {
    return try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), timeout)
        }
        true
    } catch (e: Exception) {
        false
    }
}
