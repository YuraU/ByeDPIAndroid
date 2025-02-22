package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.connection

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.lifecycle.lifecycleScope
import io.github.dovecoteescapee.byedpi.common.storage.AppStatus
import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.appStatus
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.isCmdEnable
import io.github.dovecoteescapee.byedpi.common.system.navigation.Router
import io.github.dovecoteescapee.byedpi.common.system.utils.observeFlowWithLifecycle
import io.github.dovecoteescapee.byedpi.common.system.utils.savedStateViewModel
import io.github.dovecoteescapee.byedpi.common.ui.fragment.BaseFragment
import io.github.dovecoteescapee.byedpi.common.ui.fragment.MenuProviderFragment
import io.github.dovecoteescapee.byedpi.common.ui.theme.ByeDPITheme
import io.github.dovecoteescapee.byedpi.feature.connection.ui.R
import io.github.dovecoteescapee.byedpi.feature.connection.ui.data.LogProvider
import io.github.dovecoteescapee.byedpi.feature.connection.ui.databinding.ConnectionFragmentBinding
import io.github.dovecoteescapee.byedpi.feature.connection.ui.di.ConnectionComponentHolder
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.ViewModelProvider
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.connection.ui.ConnectionScreen
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.permissionrequest.AskVpnPermissionFragment
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles.ProfilesFragment
import io.github.dovecoteescapee.byedpi.feature.settings.api.ByeDpiCommandLineSettingsScreen
import io.github.dovecoteescapee.byedpi.feature.settings.api.ByeDpiUISettingsScreen
import io.github.dovecoteescapee.byedpi.feature.settings.api.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

internal class ConnectionFragment : BaseFragment(R.layout.connection_fragment), MenuProviderFragment {

    internal lateinit var appSettings: KeyValueStorage<StorageType.AppSettings>
    internal lateinit var byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
    internal lateinit var router: Router
    internal lateinit var vmProvider: ViewModelProvider
    internal lateinit var logProvider: LogProvider

    override val component = ConnectionComponentHolder.get()

    private var vb: ConnectionFragmentBinding? = null
    private val vm: ConnectionViewModel by savedStateViewModel {
        vmProvider.getConnectionViewModel()
    }

    private val vpnRegister: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                vm.start(Mode.VPN)
            } else {
                Toast.makeText(requireContext(), R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
                vm.updateStatus()
            }
        }

    private val logsRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val logs = logProvider.collectLogs()

                if (logs == null) {
                    Toast.makeText(
                        requireActivity(),
                        R.string.logs_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val uri = it.data?.data ?: run {
                        Log.e(TAG, "No data in result")
                        return@launch
                    }
                    requireActivity().contentResolver.openOutputStream(uri)?.use {
                        try {
                            it.write(logs.toByteArray())
                        } catch (e: IOException) {
                            Log.e(TAG, "Failed to save logs", e)
                        }
                    } ?: run {
                        Log.e(TAG, "Failed to open output stream")
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vb = ConnectionFragmentBinding.inflate(inflater, container, false)
        return vb?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu(requireActivity(), viewLifecycleOwner)

        vm.onCreate()

        vb?.composeView?.setContent {
            ByeDPITheme {
                ConnectionScreen(
                    viewModel = vm,
                    openEditorClickListener = {
                        lifecycleScope.launch {
                            if (byeDpiSettings.isCmdEnable()) {
                                router.navigateTo(ByeDpiCommandLineSettingsScreen())
                            } else {
                                router.navigateTo(ByeDpiUISettingsScreen())
                            }
                        }
                    }
                )
            }
        }

        vm.actions.observeFlowWithLifecycle(viewLifecycleOwner, block = ::handleAction)
    }

    private fun handleAction(action: ConnectionViewModel.Actions) {
        when (action) {
            is ConnectionViewModel.Actions.VpnPermissionRequest -> {
                val fragment = AskVpnPermissionFragment()

                childFragmentManager.setFragmentResultListener("bottom_sheet_result", this) { _, bundle ->
                    val isContinue = bundle.getBoolean("continue", false)
                    if (isContinue) {
                        vpnRegister.launch(action.intent)
                    }
                }

                fragment.show(childFragmentManager, null)
            }

            ConnectionViewModel.Actions.ShowProfilesFragment -> {
                val fragment = ProfilesFragment()
                fragment.show(childFragmentManager, null)
            }
        }
    }

    override fun onDestroyView() {
        vb = null
        super.onDestroyView()
    }

    companion object {
        val TAG: String = ConnectionFragment::class.java.simpleName
    }

    override fun getMenuRes() = R.menu.menu_main

    override fun onMenuItemClicked(itemId: Int): Boolean {
        return when (itemId) {
            R.id.action_settings -> {
                if (appStatus.value == AppStatus.Halted) {
                    router.navigateTo(SettingsScreen())
                } else {
                    Toast.makeText(requireActivity(), R.string.settings_unavailable, Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }

            R.id.action_save_logs -> {
                logsRegister.launch(logProvider.createIntent())
                true
            }

            else -> true
        }
    }
}
