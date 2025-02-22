package io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.putCmdArgs
import io.github.dovecoteescapee.byedpi.feature.bypass.test.R
import io.github.dovecoteescapee.byedpi.feature.bypass.test.data.LogManager
import io.github.dovecoteescapee.byedpi.feature.bypass.test.data.ProxyManager
import io.github.dovecoteescapee.byedpi.feature.bypass.test.data.ProxyTestResources
import io.github.dovecoteescapee.byedpi.feature.bypass.test.domain.CheckSitesUseCase
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.ByPassTestViewModel.State.Data.CommandType
import io.github.dovecoteescapee.byedpi.feature.bypass.test.utility.GoogleVideoUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ByPassTestViewModel(
    private val checkSitesUseCase: CheckSitesUseCase,
    private val proxyTestResources: ProxyTestResources,
    private val logManager: LogManager,
    private val appSettings: KeyValueStorage<StorageType.AppSettings>,
    private val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>,
    private val proxyManager: ProxyManager,
) : ViewModel() {

    private var isProxyRunning: Boolean = false
    private var _isTesting: Boolean = false
    private var isTesting: Boolean
        get() = _isTesting
        set(value) {
            viewModelScope.launch {
                appSettings.putBoolean("is_test_running", value)
            }
            _isTesting = value
        }

    private val commands: MutableList<CommandType> = mutableListOf()

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    private var testJob: Job? = null

    init {
        viewModelScope.launch {
            isTesting = appSettings.getBoolean("is_test_running")

            if (isTesting) {
                _state.emit(State.Error)
                isTesting = false
            } else {
                val previousLogs = logManager.loadLog()
                if (previousLogs.isNotEmpty()) {
                    _state.emit(
                        State.Data(
                            inProgress = false,
                            btnTextRes = R.string.test_start,
                            progress = State.Data.ProgressType.Content(
                                resId = R.string.test_complete
                            ),
                            commands = logManager.parseLog(previousLogs),
                            successfulCmds = mutableListOf(),
                        )
                    )
                } else {
                    _state.emit(State.Empty)
                }
            }
        }
    }

    fun btnClicked() {
        if (isTesting) {
            stopTesting()
        } else {
            startTesting()
        }
    }

    private fun startTesting() {
        if (testJob?.isActive == true) {
            isTesting = true
            return
        }

        testJob = viewModelScope.launch(Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
            _state.tryEmit(State.Error)
        }) {
            isTesting = true

            _state.emit(
                State.Data(
                    inProgress = true,
                    btnTextRes = R.string.test_stop,
                    progress = State.Data.ProgressType.Empty,
                    commands = mutableListOf(),
                    successfulCmds = mutableListOf(),
                )
            )

            val sites = proxyTestResources.loadSites().toMutableList()
            val cmds = proxyTestResources.loadCmds()

            logManager.clearLog()

            val useGeneratedGoogleDomain = !appSettings.getBoolean("byedpi_proxytest_gdomain")

            if (useGeneratedGoogleDomain) {
                val googleVideoDomain = GoogleVideoUtils().generateGoogleVideoDomain()
                if (googleVideoDomain != null) {
                    sites.add(googleVideoDomain)
                    // appendTextToResults("--- $googleVideoDomain ---\n\n")
                    Log.i("TestActivity", "Added auto-generated Google domain: $googleVideoDomain")
                } else {
                    Log.e("TestActivity", "Failed to generate Google domain")
                }
            }

            val successfulCmds = mutableListOf<Pair<String, Int>>()

            for ((index, cmd) in cmds.withIndex()) {
                val cmdIndex = index + 1

                _state.emit(
                    (_state.value as State.Data).copy(
                        progress = State.Data.ProgressType.Content(
                            resId = R.string.test_process,
                            formatArgs = arrayOf(cmdIndex, cmds.size),
                        )
                    )
                )

                launch(Dispatchers.IO) {
                    val code = proxyManager.startProxyService(cmd)

                    isProxyRunning = false

                    if (code != 0) {
                        throw Exception()
                    }
                }

                if (!proxyManager.waitForProxyStatus()) {
                    _state.emit(State.Error)
                    return@launch
                }

                isProxyRunning = true

                commands.add(checkSitesUseCase.invoke(cmd, sites))

                updateCommands()

                if (proxyManager.stopProxyService() < 0 && proxyManager.waitForProxyStatus(500L)) {
                    throw Exception()
                }

                isProxyRunning = false
            }

            commands.forEach { command ->
                val percentage = extractPercentage(command.results)
                if (percentage != null && percentage >= 50) {
                    successfulCmds.add(command.cmd to percentage)
                }
            }

            successfulCmds.sortByDescending { it.second }

            val successfulCmdList = successfulCmds.map {
                val (cmd, success) = it
                CommandType(
                    cmd = cmd,
                    results = "$success%"
                )
            }

            _state.emit(
                (_state.value as State.Data).copy(
                    progress = State.Data.ProgressType.Content(
                        resId = R.string.test_complete
                    ),
                    commands = commands,
                    successfulCmds = successfulCmdList
                )
            )

            stopTesting()
        }
    }

    private fun stopTesting() {
        isTesting = false
        testJob?.cancel()

        viewModelScope.launch {
            _state.emit(
                (_state.value as State.Data).copy(
                    inProgress = false,
                    btnTextRes = R.string.test_start
                )
            )

            if (isProxyRunning) {
                proxyManager.stopProxyService()
            }
        }
    }

    private suspend fun updateCommands() {
        _state.emit(
            (_state.value as State.Data).copy(
                commands = commands
            )
        )
    }

    private fun extractPercentage(input: String): Int? {
        val regex = """\((\d+)%\)""".toRegex()
        val matchResult = regex.find(input)
        return matchResult?.groups?.get(1)?.value?.toInt()
    }

    suspend fun updateCmdInPreferences(cmd: String) {
        byeDpiSettings.putCmdArgs(cmd)
    }

    sealed interface State {
        data object Loading : State
        data object Empty : State
        data object Error : State
        data class Data(
            val inProgress: Boolean,
            val btnTextRes: Int,
            val progress: ProgressType,
            val commands: List<CommandType>,
            val successfulCmds: List<CommandType>,
        ) : State {
            sealed interface ProgressType {
                data object Empty : ProgressType
                class Content(
                    val resId: Int,
                    vararg val formatArgs: Any?,
                ) : ProgressType
            }

            class CommandType(
                val cmd: String,
                val results: String,
                val sites: List<String>? = null
            )
        }
    }
}
