package io.github.dovecoteescapee.byedpi.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.SystemClock
import io.github.dovecoteescapee.byedpi.bypass.services.ServiceManager
import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.ContextComponent
import io.github.dovecoteescapee.byedpi.common.system.components.ContextComponentHolder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_REBOOT ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // for A15, todo: use wasForceStopped
            if (SystemClock.elapsedRealtime() > 5 * 60 * 1000) {
                return
            }

            initComponents(context.applicationContext)

            val appSettings = StorageComponentHolder.get().appSettings()

            GlobalScope.launch {
                appSettings.getString("byedpi_mode")?.let {
                    val mode = Mode.fromString(it)
                    val autorunEnabled = appSettings.getBoolean("autostart")

                    if (autorunEnabled) {
                        when (mode) {
                            Mode.VPN -> {
                                if (VpnService.prepare(context) == null) {
                                    ServiceManager.startVpn(context)
                                }
                            }

                            Mode.Proxy -> {
                                ServiceManager.startProxy(context)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initComponents(context: Context) {
        ContextComponentHolder.set { ContextComponent(context) }
    }
}
