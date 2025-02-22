package io.github.dovecoteescapee.byedpi.feature.settings.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.takisoft.preferencex.EditTextPreference
import io.github.dovecoteescapee.byedpi.common.ui.utils.findPreferenceNotNull
import io.github.dovecoteescapee.byedpi.feature.settings.R
import io.github.dovecoteescapee.byedpi.common.storage.utils.Command
import io.github.dovecoteescapee.byedpi.common.storage.data.SettingsDataStore
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.ui.fragment.BasePreferenceFragment
import io.github.dovecoteescapee.byedpi.feature.settings.di.AppSettingsComponentHolder
import io.github.dovecoteescapee.byedpi.common.storage.utils.HistoryUtils
import io.github.dovecoteescapee.byedpi.common.storage.utils.getCmdArgs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal class ByeDpiCommandLineSettingsFragment : BasePreferenceFragment() {

    lateinit var appSettings: KeyValueStorage<StorageType.AppSettings>
    lateinit var byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>

    override val component = AppSettingsComponentHolder.get()

    private val dataStoreChangeListener = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private lateinit var cmdHistoryUtils: HistoryUtils
    private lateinit var editTextPreference: EditTextPreference
    private lateinit var historyCategory: PreferenceCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            dataStoreChangeListener
                .collect()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsDataStore(
            WeakReference(lifecycleScope),
            byeDpiSettings as KeyValueStorage<StorageType>,
        )
        setPreferencesFromResource(R.xml.byedpi_cmd_settings, rootKey)

        cmdHistoryUtils = HistoryUtils(appSettings)

        editTextPreference = findPreferenceNotNull("byedpi_cmd_args")
        historyCategory = findPreferenceNotNull("cmd_history_category")

        editTextPreference.setOnPreferenceChangeListener { _, newValue ->
            lifecycleScope.launch {
                val newCmd = newValue.toString()
                if (newCmd.isNotBlank()) {
                    byeDpiSettings.getCmdArgs()?.let {
                        cmdHistoryUtils.addCommand(it)
                    }
                }
                updateHistoryCategory()
            }

            true
        }

        lifecycleScope.launch {
            updateHistoryCategory()
        }
    }

    private suspend fun updateHistoryCategory() {
        historyCategory.removeAll()
        val history = cmdHistoryUtils.getHistory()

        history.sortedWith(compareByDescending<Command> { it.pinned }.thenBy { history.indexOf(it) })
            .forEach { command ->
                val preference = createPreference(command)
                historyCategory.addPreference(preference)
            }
    }

    private fun createPreference(command: Command) =
        Preference(requireContext()).apply {
            title = command.text
            summary = buildSummary(command)
            setOnPreferenceClickListener {
                showActionDialog(command)
                true
            }
        }

    private fun buildSummary(command: Command): String {
        val summary = StringBuilder()
        if (command.name != null) {
            summary.append(command.name)
        }
        if (command.pinned) {
            if (summary.isNotEmpty()) summary.append(" - ")
            summary.append(context?.getString(R.string.cmd_history_pinned))
        }
        return summary.toString()
    }

    private fun showActionDialog(command: Command) {
        val options = arrayOf(
            getString(R.string.cmd_history_apply),
            if (command.pinned) getString(R.string.cmd_history_unpin) else getString(R.string.cmd_history_pin),
            getString(R.string.cmd_history_rename),
            getString(R.string.cmd_history_copy),
            getString(R.string.cmd_history_delete)
        )

        MaterialAlertDialogBuilder(requireContext(), io.github.dovecoteescapee.byedpi.common.ui.R.style.AlertDialogTheme)
            .setTitle(getString(R.string.cmd_history_menu))
            .setItems(options) { _, which ->
                lifecycleScope.launch {
                    when (which) {
                        0 -> applyCommand(command.text)
                        1 -> if (command.pinned) unpinCommand(command.text) else pinCommand(command.text)
                        2 -> showRenameDialog(command)
                        3 -> copyToClipboard(command.text)
                        4 -> deleteCommand(command.text)
                    }
                }
            }
            .show()
    }

    private fun showRenameDialog(command: Command) {
        val input = EditText(requireContext()).apply {
            setText(command.name)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(input)
        }

        MaterialAlertDialogBuilder(requireContext(), io.github.dovecoteescapee.byedpi.common.ui.R.style.AlertDialogTheme)
            .setTitle(getString(R.string.cmd_history_rename))
            .setView(container)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    lifecycleScope.launch {
                        cmdHistoryUtils.renameCommand(command.text, newName)
                        updateHistoryCategory()
                    }
                }
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    private fun applyCommand(command: String) {
        editTextPreference.text = command
    }

    private suspend fun pinCommand(command: String) {
        cmdHistoryUtils.pinCommand(command)
        updateHistoryCategory()
    }

    private suspend fun unpinCommand(command: String) {
        cmdHistoryUtils.unpinCommand(command)
        updateHistoryCategory()
    }

    private suspend fun deleteCommand(command: String) {
        cmdHistoryUtils.deleteCommand(command)
        updateHistoryCategory()
    }

    private fun copyToClipboard(command: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Command", command)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        val TAG = ByeDpiCommandLineSettingsFragment::class.java.simpleName
    }
}
