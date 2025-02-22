package io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.SwitchPreference
import io.github.dovecoteescapee.byedpi.common.storage.data.SettingsDataStore
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.ui.fragment.BasePreferenceFragment
import io.github.dovecoteescapee.byedpi.common.ui.utils.findPreferenceNotNull
import io.github.dovecoteescapee.byedpi.feature.bypass.test.R
import io.github.dovecoteescapee.byedpi.feature.bypass.test.di.ByPassTestComponentHolder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal class ProxyTestSettingsFragment : BasePreferenceFragment() {

    lateinit var appSettings: KeyValueStorage<StorageType.AppSettings>

    override val component = ByPassTestComponentHolder.get()

    private val dataStoreChangeListener = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)

        viewLifecycleOwner.lifecycleScope.launch {
            dataStoreChangeListener
                .onEach { updatePreferences() }
                .collect()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsDataStore(
            WeakReference(lifecycleScope),
            appSettings as KeyValueStorage<StorageType>,
            dataStoreChangeListener
        )

        setPreferencesFromResource(R.xml.proxy_test_settings, rootKey)
        updatePreferences()
    }

    private fun updatePreferences() {
        val switchUserDomains = findPreferenceNotNull<SwitchPreference>("byedpi_proxytest_userdomains")
        val switchUserCommands = findPreferenceNotNull<SwitchPreference>("byedpi_proxytest_usercommands")
        val textUserDomains = findPreferenceNotNull<EditTextPreference>("byedpi_proxytest_domains")
        val textUserCommands = findPreferenceNotNull<EditTextPreference>("byedpi_proxytest_commands")

        val setUserDomains = { enable: Boolean -> textUserDomains.isEnabled = enable }
        val setUserCommands = { enable: Boolean -> textUserCommands.isEnabled = enable }

        setUserDomains(switchUserDomains.isChecked)
        setUserCommands(switchUserCommands.isChecked)
    }

    companion object {
        val TAG = ProxyTestSettingsFragment::class.java.simpleName
    }
}
