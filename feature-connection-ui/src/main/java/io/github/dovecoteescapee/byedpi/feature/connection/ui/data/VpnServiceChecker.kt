package io.github.dovecoteescapee.byedpi.feature.connection.ui.data

import android.content.Context
import android.content.Intent
import android.net.VpnService

class VpnServiceChecker(
    private val ctx: Context
) {

    fun checkNeedVpnPermissionRequest(): Intent? {
        return VpnService.prepare(ctx)
    }
}
