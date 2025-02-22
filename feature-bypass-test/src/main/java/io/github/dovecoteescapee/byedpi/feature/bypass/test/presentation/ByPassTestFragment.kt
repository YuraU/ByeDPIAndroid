package io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import io.github.dovecoteescapee.byedpi.common.storage.utils.HistoryUtils
import io.github.dovecoteescapee.byedpi.common.system.navigation.Router
import io.github.dovecoteescapee.byedpi.common.system.utils.savedStateViewModel
import io.github.dovecoteescapee.byedpi.common.ui.fragment.BaseFragment
import io.github.dovecoteescapee.byedpi.common.ui.fragment.MenuProviderFragment
import io.github.dovecoteescapee.byedpi.feature.bypass.test.R
import io.github.dovecoteescapee.byedpi.feature.bypass.test.api.ProxyTestSettingsScreen
import io.github.dovecoteescapee.byedpi.feature.bypass.test.databinding.ByPassTestFragmentBinding
import io.github.dovecoteescapee.byedpi.feature.bypass.test.di.ByPassTestComponentHolder
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.ByPassTestViewModel.State
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class ByPassTestFragment : BaseFragment(R.layout.by_pass_test_fragment), MenuProviderFragment {

    private val cmdHistoryUtils: HistoryUtils by lazy {
        HistoryUtils(StorageComponentHolder.get().appSettings())
    }
    internal lateinit var vmProvider: ViewModelProvider
    internal lateinit var router: Router

    override val component = ByPassTestComponentHolder.get()

    private var vb: ByPassTestFragmentBinding? = null
    private val vm: ByPassTestViewModel by savedStateViewModel {
        vmProvider.getByPassTestViewModel()
    }

    private val logClickable by lazy {
        runBlocking {
            StorageComponentHolder.get().appSettings()
                .getBoolean("byedpi_proxytest_logclickable")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)

        requireActivity().requestedOrientation = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            Configuration.ORIENTATION_PORTRAIT -> {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            else -> {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vb = ByPassTestFragmentBinding.inflate(inflater, container, false)
        return vb?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu(requireActivity(), viewLifecycleOwner)

        vb?.startStopButton?.setOnClickListener {
            vm.btnClicked()
        }

        vb?.resultsTextView?.movementMethod = LinkMovementMethod.getInstance()

        lifecycleScope.launch {
            vm.state
                .onEach { handleState(it) }
                .collect()
        }

        lifecycleScope.launch {
            vm.state
                .filter { it is State.Data }
                .map { (it as State.Data).btnTextRes }
                .distinctUntilChanged()
                .collect { newRes ->
                    vb?.startStopButton?.text = getString(newRes)
                }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if ((vm.state.value is State.Data) && (vm.state.value as State.Data).inProgress) {
                    Toast.makeText(requireActivity(), "Остановите проверку", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    router.goBack()
                }
            }
        })
    }

    override fun onDestroy() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        super.onDestroy()
    }

    override fun getMenuRes() = R.menu.menu_test

    override fun onMenuItemClicked(itemId: Int): Boolean {
        return when (itemId) {
            R.id.action_bypass_test_settings -> {
                if (vm.state.value is State.Data && (vm.state.value as State.Data).inProgress) {
                    Toast.makeText(requireContext(), R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                } else {
                    router.navigateTo(ProxyTestSettingsScreen())
                }

                true
            }

            else -> true
        }
    }

    private fun handleState(state: State) {
        when (state) {
            is State.Data -> {
                when (val progress = state.progress) {
                    is State.Data.ProgressType.Content -> {
                        vb?.progressTextView?.text = getString(progress.resId, *progress.formatArgs)
                    }
                    State.Data.ProgressType.Empty -> {
                        vb?.progressTextView?.text = ""
                    }
                }

                if (state.commands.isEmpty()) {
                    vb?.resultsTextView?.text = ""
                } else {
                    if (state.inProgress) {
                        val command = state.commands.last()
                        appendCmdToResults("${command.cmd}\n")
                        command.sites?.let { sites ->
                            appendTextToResults("\n")
                            sites.forEach { appendTextToResults("${it}\n") }
                            appendTextToResults("\n")
                        }
                        appendTextToResults("${command.results}\n\n")
                    } else {
                        if (vb?.resultsTextView?.text?.isEmpty() == true) {
                            state.commands.forEach { command ->
                                appendCmdToResults("${command.cmd}\n")
                                command.sites?.let { sites ->
                                    sites.forEach { appendTextToResults("${it}\n") }
                                }
                                appendTextToResults("${command.results}\n\n")
                            }
                        }

                        if (state.successfulCmds.isNotEmpty()) {
                            appendTextToResults("${getString(R.string.test_good_cmds)}\n\n")
                            state.successfulCmds.forEachIndexed { index, commandType ->
                                appendTextToResults("${index + 1}. ")
                                appendLinkToResults("${commandType.cmd}\n")
                                appendTextToResults("${commandType.results}\n\n")
                            }

                            appendTextToResults(getString(R.string.test_complete_info))
                        } else {
                            Unit
                        }
                    }
                }

            }
            State.Empty -> {
                appendTextToResults(getString(R.string.test_disclaimer))
            }
            State.Error -> {
                vb?.progressTextView?.text = getString(R.string.test_proxy_error)
                vb?.resultsTextView?.text = getString(R.string.test_crash)
                vb?.startStopButton?.text = getString(R.string.test_start)
            }
            State.Loading -> Unit
        }
    }

    private fun appendLinkToResults(text: String) {
        val spannableString = SpannableString(text)
        val menuItems = arrayOf(
            getString(R.string.cmd_history_apply),
            getString(R.string.cmd_history_copy)
        )

        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    MaterialAlertDialogBuilder(requireContext(), io.github.dovecoteescapee.byedpi.common.ui.R.style.AlertDialogTheme)
                        .setTitle(getString(R.string.cmd_history_menu))
                        .setItems(menuItems) { _, which ->
                            when (which) {
                                0 -> addToHistory(text.trim())
                                1 -> copyToClipboard(text.trim())
                            }
                        }
                        .show()
                }
            },
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        vb?.resultsTextView?.append(spannableString)
        scrollToBottom()
    }

    private fun appendTextToResults(text: String) {
        vb?.resultsTextView?.append(text)
        scrollToBottom()
    }

    private fun appendCmdToResults(text: String) {
        if (logClickable) {
            appendLinkToResults(text)
        } else {
            appendTextToResults(text)
        }
    }

    private fun scrollToBottom() {
        vb?.scrollView?.post {
            vb?.scrollView?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun addToHistory(command: String) {
        lifecycleScope.launch {
            vm.updateCmdInPreferences(command)
            cmdHistoryUtils.addCommand(command)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("command", text)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        val TAG = ByPassTestFragment::class.java.simpleName
    }
}
