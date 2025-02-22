package io.github.dovecoteescapee.byedpi

import android.net.VpnService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.dovecoteescapee.byedpi.bypass.services.ServiceManager
import io.github.dovecoteescapee.byedpi.common.storage.AppStatus
import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.appMode
import io.github.dovecoteescapee.byedpi.common.storage.appStatus

class ToggleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toggleService()
        finish()
    }

    private fun toggleService() {
        val status = appStatus.value
        val mode = appMode.value
        when (status) {
            AppStatus.Halted -> {
                when (mode) {
                    Mode.Proxy -> {
                        ServiceManager.startProxy(this)
                    }
                    Mode.VPN -> {
                        if (VpnService.prepare(this) != null) {
                            return
                        }

                        ServiceManager.startVpn(this)
                    }
                }
            }

            AppStatus.Running -> {
                when (mode) {
                    Mode.Proxy -> ServiceManager.stopProxy(this)
                    Mode.VPN -> ServiceManager.stopVpn(this)
                }
            }

            AppStatus.Starting -> Unit
        }
    }

    companion object {
        private const val TAG = "ToggleActivity"
    }
}
