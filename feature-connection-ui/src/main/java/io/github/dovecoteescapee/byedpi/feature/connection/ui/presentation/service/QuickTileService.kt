package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.service

import android.content.ComponentName
import android.content.Context
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import io.github.dovecoteescapee.byedpi.common.storage.AppStatus
import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.appMode
import io.github.dovecoteescapee.byedpi.common.storage.appStatus
import io.github.dovecoteescapee.byedpi.feature.connection.ui.di.ConnectionComponentHolder
import io.github.dovecoteescapee.byedpi.feature.connection.ui.domain.ConnectionManagerUseCase

@RequiresApi(Build.VERSION_CODES.N)
class QuickTileService : TileService() {

    internal lateinit var connectionManagerUseCase: ConnectionManagerUseCase

    override fun onCreate() {
        ConnectionComponentHolder.get().inject(this)
        super.onCreate()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateStatus()
    }

    override fun onClick() {
        if (qsTile.state == Tile.STATE_UNAVAILABLE) return

        unlockAndRun { handleClick() }
    }

    private fun handleClick() {
        val status= appStatus.value
        val mode = appMode.value
        when (status) {
            AppStatus.Halted -> {
                if (mode == Mode.VPN && VpnService.prepare(this) != null) {
                    return
                }

                connectionManagerUseCase.start(mode)
                setState(Tile.STATE_ACTIVE)
            }
            AppStatus.Running -> {
                connectionManagerUseCase.stop(mode)
                setState(Tile.STATE_INACTIVE)
            }

            AppStatus.Starting -> Unit
        }

        Log.i(TAG, "Toggle tile")
        updateTile(this)
    }

    private fun updateStatus() {
        val status = appStatus.value

        if (status == AppStatus.Running) {
            setState(Tile.STATE_ACTIVE)
        } else {
            setState(Tile.STATE_INACTIVE)
        }
    }

    private fun setState(newState: Int) {
        qsTile.apply {
            state = newState
            updateTile()
        }
    }

    companion object {
        private const val TAG = "QuickTileService"

        fun updateTile(context: Context) {
            requestListeningState(context, ComponentName(context, QuickTileService::class.java))
        }
    }
}
