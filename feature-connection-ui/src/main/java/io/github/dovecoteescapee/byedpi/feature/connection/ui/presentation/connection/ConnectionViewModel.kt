package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.connection

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dovecoteescapee.byedpi.bypass.feature.api.fromKeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.AppStatus
import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.appMode
import io.github.dovecoteescapee.byedpi.common.storage.appStatus
import io.github.dovecoteescapee.byedpi.common.storage.byeDpiProxyArgs
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfile
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfilesList
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProxyIpAndPort
import io.github.dovecoteescapee.byedpi.common.storage.utils.setProfile
import io.github.dovecoteescapee.byedpi.feature.connection.ui.R
import io.github.dovecoteescapee.byedpi.feature.connection.ui.data.VpnServiceChecker
import io.github.dovecoteescapee.byedpi.feature.connection.ui.domain.ConnectionManagerUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class ConnectionViewModel(
    private val appSettings: KeyValueStorage<StorageType.AppSettings>,
    private val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>,
    private val connectionManagerUseCase: ConnectionManagerUseCase,
    private val vpnServiceChecker: VpnServiceChecker,
) : ViewModel() {

    private var selectedProfilePosition: Int = 0
    private val profiles: MutableList<String> = mutableListOf()

    private val _actions: MutableSharedFlow<Actions> = MutableSharedFlow(extraBufferCapacity = 1)
    val actions: Flow<Actions> = _actions.asSharedFlow()

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (appSettings.getBoolean("auto_connect") && appStatus.value != AppStatus.Running) {
                start(appMode.value)
            }
        }

        viewModelScope.launch {
            appStatus
                .onEach { updateStatus() }
                .collect()
        }

        viewModelScope.launch {
            appMode
                .onEach { updateStatus() }
                .collect()
        }
    }

    fun onCreate() {
        viewModelScope.launch {
            profiles.clear()
            profiles.addAll(appSettings.getProfilesList().toMutableList())
            val profile = appSettings.getProfile()
            selectedProfilePosition = profiles.indexOf(profiles.find { it == profile })

            profiles.add("Добавить/Редактировать")

            updateStatus()
        }
    }

    fun connectedClick() {
        when (appStatus.value) {
            AppStatus.Halted -> start(appMode.value)
            AppStatus.Running -> stop()
            AppStatus.Starting -> updateStatus()
        }
    }

    fun start(mode: Mode) {
        when (mode) {
            Mode.VPN -> {
                val intentPrepare = vpnServiceChecker.checkNeedVpnPermissionRequest()
                if (intentPrepare != null) {
                    _actions.tryEmit(Actions.VpnPermissionRequest(intentPrepare))
                } else {
                    connectionManagerUseCase.start(Mode.VPN)
                }
            }

            Mode.Proxy -> connectionManagerUseCase.start(Mode.Proxy)
        }
    }

    fun updateStatus() {
        val status = appStatus.value
        val mode = appMode.value

        Log.i(TAG, "Updating status: $status, $mode")
        viewModelScope.launch {
            when (status) {
                AppStatus.Halted -> {
                    val (ip, port) = byeDpiSettings.getProxyIpAndPort()

                    val statusTextRes = when (mode) {
                        Mode.Proxy -> R.string.proxy_down
                        Mode.VPN -> R.string.vpn_disconnected
                    }

                    val statusBtnRes = when (mode) {
                        Mode.Proxy -> R.string.proxy_start
                        Mode.VPN -> R.string.vpn_connect
                    }

                    _state.tryEmit(
                        State.Data(
                            profiles = profiles,
                            selectedProfilePosition = selectedProfilePosition,
                            proxyPort = port,
                            proxyIp = ip,
                            statusTextRes = statusTextRes,
                            statusBtnRes = statusBtnRes,
                        )
                    )
                }

                AppStatus.Running -> {
                    when (mode) {
                        Mode.VPN -> {
                            _state.tryEmit(
                                if (_state.value is State.Data) {
                                    (_state.value as State.Data).copy(
                                        statusTextRes = R.string.vpn_connected,
                                        statusBtnRes = R.string.vpn_disconnect,
                                    )
                                } else {
                                    val (ip, port) = byeDpiSettings.getProxyIpAndPort()
                                    State.Data(
                                        profiles = profiles,
                                        selectedProfilePosition = selectedProfilePosition,
                                        proxyPort = port,
                                        proxyIp = ip,
                                        statusTextRes = R.string.vpn_connected,
                                        statusBtnRes = R.string.vpn_disconnect,
                                    )
                                }
                            )
                        }

                        Mode.Proxy -> {
                            _state.tryEmit(
                                if (_state.value is State.Data) {
                                    (_state.value as State.Data).copy(
                                        statusTextRes = R.string.proxy_up,
                                        statusBtnRes = R.string.proxy_stop,
                                    )
                                } else {
                                    val (ip, port) = byeDpiSettings.getProxyIpAndPort()
                                    State.Data(
                                        profiles = profiles,
                                        selectedProfilePosition = selectedProfilePosition,
                                        proxyPort = port,
                                        proxyIp = ip,
                                        statusTextRes = R.string.proxy_up,
                                        statusBtnRes = R.string.proxy_stop,
                                    )
                                }
                            )
                        }
                    }
                }

                AppStatus.Starting -> {
                    _state.tryEmit(
                        (_state.value as State.Data).copy(
                            statusTextRes = R.string.connecting,
                        )
                    )
                }
            }
        }
    }

    fun profileSelected(index: Int) {
        if (index == profiles.size - 1) {
            _state.tryEmit(
                (_state.value as State.Data).copy(
                    selectedProfilePosition = selectedProfilePosition,
                )
            )
            _actions.tryEmit(Actions.ShowProfilesFragment)
        } else {
            viewModelScope.launch {
                selectedProfilePosition = index

                appSettings.setProfile(profiles[index])
                byeDpiProxyArgs = fromKeyValueStorage(byeDpiSettings)

                _state.tryEmit(
                    (_state.value as State.Data).copy(
                        selectedProfilePosition = index,
                    )
                )
            }
        }
    }

    private fun stop() {
        connectionManagerUseCase.stop(appMode.value)
    }

    sealed interface Actions {
        class VpnPermissionRequest(val intent: Intent) : Actions
        data object ShowProfilesFragment : Actions
    }

    sealed interface State {
        data object Empty : State
        data class Data(
            val profiles: List<String>,
            val selectedProfilePosition: Int,
            val proxyIp: String,
            val proxyPort: String,
            val statusTextRes: Int,
            val statusBtnRes: Int,
        ) : State
    }

    companion object {
        private val TAG: String = ConnectionViewModel::class.java.simpleName
    }
}
