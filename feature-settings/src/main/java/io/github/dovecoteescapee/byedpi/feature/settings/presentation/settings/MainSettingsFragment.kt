package io.github.dovecoteescapee.byedpi.feature.settings.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreference
import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.byeDpiProxyArgs
import io.github.dovecoteescapee.byedpi.common.system.navigation.Router
import io.github.dovecoteescapee.byedpi.common.ui.utils.findPreferenceNotNull
import io.github.dovecoteescapee.byedpi.feature.bypass.test.api.ByPassTestScreen
import io.github.dovecoteescapee.byedpi.feature.settings.R
import io.github.dovecoteescapee.byedpi.feature.settings.api.AppSelectionScreen
import io.github.dovecoteescapee.byedpi.feature.settings.api.ByeDpiCommandLineSettingsScreen
import io.github.dovecoteescapee.byedpi.feature.settings.api.ByeDpiUISettingsScreen
import io.github.dovecoteescapee.byedpi.common.storage.data.SettingsDataStore
import io.github.dovecoteescapee.byedpi.common.storage.data.byedpi.ByeDpiProxyCmdPreferences
import io.github.dovecoteescapee.byedpi.common.storage.data.byedpi.ByeDpiProxyUIPreferences
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.setMode
import io.github.dovecoteescapee.byedpi.common.system.data.AppInfo
import io.github.dovecoteescapee.byedpi.common.ui.fragment.BasePreferenceFragment
import io.github.dovecoteescapee.byedpi.common.ui.fragment.MenuProviderFragment
import io.github.dovecoteescapee.byedpi.feature.settings.data.SettingsManager
import io.github.dovecoteescapee.byedpi.feature.settings.di.AppSettingsComponentHolder
import io.github.dovecoteescapee.byedpi.feature.settings.utils.checkIp
import io.github.dovecoteescapee.byedpi.feature.settings.utils.checkNotLocalIp
import io.github.dovecoteescapee.byedpi.feature.settings.utils.setEditTestPreferenceListenerPort
import io.github.dovecoteescapee.byedpi.feature.settings.utils.setEditTextPreferenceListener
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal class MainSettingsFragment : BasePreferenceFragment(), MenuProviderFragment {

    private val dataStoreChangeListener = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val appSettingsDataStore by lazy {
        SettingsDataStore(
            WeakReference(lifecycleScope),
            appSettings as KeyValueStorage<StorageType>,
            dataStoreChangeListener
        )
    }

    private val byeDpiSettingsDataStore by lazy {
        SettingsDataStore(
            WeakReference(lifecycleScope),
            byeDpiSettings as KeyValueStorage<StorageType>,
            dataStoreChangeListener
        )
    }

    override val component = AppSettingsComponentHolder.get()

    internal lateinit var appInfo: AppInfo
    internal lateinit var router: Router
    internal lateinit var appSettings: KeyValueStorage<StorageType.AppSettings>
    internal lateinit var byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
    internal lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu(requireActivity(), viewLifecycleOwner)

        lifecycleScope.launch {
            dataStoreChangeListener
                .onEach { key -> updatePreferences(key) }
                .collect()
        }
    }

    private fun initGeneralBlock() {
        setEditTextPreferenceListener("dns_ip") {
            it.isBlank() || checkNotLocalIp(it)
        }

        findPreferenceNotNull<Preference>("selected_apps")
            .setOnPreferenceClickListener {
                router.navigateTo(AppSelectionScreen())
                false
            }
    }

    private fun initByeDpiCategory() {
        findPreferenceNotNull<Preference>("byedpi_ui_settings")
            .setOnPreferenceClickListener {
                router.navigateTo(ByeDpiUISettingsScreen())
                true
            }

        findPreferenceNotNull<Preference>("byedpi_cmd_settings")
            .setOnPreferenceClickListener {
                router.navigateTo(ByeDpiCommandLineSettingsScreen())
                true
            }

        findPreferenceNotNull<Preference>("proxy_test")
            .setOnPreferenceClickListener {
                router.navigateTo(ByPassTestScreen())
                true
            }
    }

    private fun initAboutCategory() {
        findPreferenceNotNull<Preference>("telegram_group")
            .intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(appInfo.telegramLink)
        )

        findPreferenceNotNull<Preference>("source_code")
            .intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(appInfo.sourceCodeLink)
        )

        findPreferenceNotNull<Preference>("version").summary = appInfo.appVersion
        findPreferenceNotNull<Preference>("byedpi_version").summary = appInfo.byeDpiVersion
    }

    private fun initByeDpiProxyCategory() {
        setEditTextPreferenceListener("byedpi_proxy_ip") { checkIp(it) }
        setEditTestPreferenceListenerPort("byedpi_proxy_port")
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_settings, rootKey)

        setDataStores()

        initGeneralBlock()
        initByeDpiCategory()
        initByeDpiProxyCategory()
        initAboutCategory()

        val switchCmdSettings = findPreferenceNotNull<SwitchPreference>("byedpi_enable_cmd_settings")
        val uiSettings = findPreferenceNotNull<Preference>("byedpi_ui_settings")
        val cmdSettings = findPreferenceNotNull<Preference>("byedpi_cmd_settings")
        val proxyTest = findPreferenceNotNull<Preference>("proxy_test")

        val setByeDpiSettingsMode = { enable: Boolean ->
            uiSettings.isEnabled = !enable
            cmdSettings.isEnabled = enable
            proxyTest.isEnabled = enable
        }

        setByeDpiSettingsMode(switchCmdSettings.isChecked)

        switchCmdSettings.setOnPreferenceChangeListener { _, newValue ->
            setByeDpiSettingsMode(newValue as Boolean)
            true
        }
    }

    private fun setDataStores() {
        findPreferenceNotNull<Preference>("language").preferenceDataStore = appSettingsDataStore
        findPreferenceNotNull<Preference>("app_theme").preferenceDataStore = appSettingsDataStore
        findPreferenceNotNull<Preference>("byedpi_mode").preferenceDataStore = appSettingsDataStore
        findPreferenceNotNull<Preference>("ipv6_enable").preferenceDataStore = appSettingsDataStore
        findPreferenceNotNull<Preference>("applist_type").preferenceDataStore = appSettingsDataStore
        findPreferenceNotNull<Preference>("autostart").preferenceDataStore = appSettingsDataStore
        findPreferenceNotNull<Preference>("auto_connect").preferenceDataStore = appSettingsDataStore
        findPreferenceNotNull<Preference>("byedpi_enable_cmd_settings").preferenceDataStore = byeDpiSettingsDataStore

        findPreferenceNotNull<Preference>("dns_ip").preferenceDataStore = byeDpiSettingsDataStore
        findPreferenceNotNull<Preference>("byedpi_proxy_ip").preferenceDataStore = byeDpiSettingsDataStore
        findPreferenceNotNull<Preference>("byedpi_proxy_port").preferenceDataStore = byeDpiSettingsDataStore

        preferenceScreen?.let { screen ->
            for (i in 0 until screen.preferenceCount) {
                val preference = screen.getPreference(i)

                if (preference is PreferenceCategory) {
                    for (j in 0 until preference.preferenceCount) {
                        val child = preference.getPreference(j)

                        when (child) {
                            is EditTextPreference -> {
                                val dataStore = child.preferenceDataStore
                                if (dataStore != null) {
                                    child.text = dataStore.getString(child.key, child.text)
                                }
                            }
                            is SwitchPreference -> {
                                val dataStore = child.preferenceDataStore
                                if (dataStore != null) {
                                    child.isChecked = dataStore.getBoolean(child.key, child.isChecked)
                                }
                            }
                            is ListPreference -> {
                                val dataStore = child.preferenceDataStore
                                if (dataStore != null) {
                                    child.value = dataStore.getString(child.key, child.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePreferences(null)
    }

    override fun getMenuRes() = R.menu.menu_settings

    override fun onMenuItemClicked(itemId: Int): Boolean {
        return when (itemId) {
            R.id.action_reset_settings -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    appSettings.clear()
                    byeDpiSettings.clear()
                    router.goBack()
                }

                true
            }
            R.id.action_export_settings -> {
                val fileName = "bbd_${System.currentTimeMillis().toReadableDateTime()}.json"
                exportSettingsLauncher.launch(fileName)
                true
            }
            R.id.action_import_settings -> {
                importSettingsLauncher.launch(arrayOf("application/json"))
                true
            }

            else -> true
        }
    }

    private fun updatePreferences(key: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (key == "language" || key == "app_theme") {
                requireActivity().recreate()
                return@launch
            }

            val mode = findPreferenceNotNull<ListPreference>("byedpi_mode").value.let { Mode.fromString(it) }
            val dns = findPreferenceNotNull<EditTextPreference>("dns_ip")
            val ipv6 = findPreferenceNotNull<SwitchPreference>("ipv6_enable")
            val proxy = findPreferenceNotNull<PreferenceCategory>("byedpi_proxy_category")

            val applistType = findPreferenceNotNull<ListPreference>("applist_type")
            val selectedApps = findPreferenceNotNull<Preference>("selected_apps")

            setMode(mode)

            when (mode) {
                Mode.VPN -> {
                    dns.isVisible = true
                    ipv6.isVisible = true

                    when (applistType.value) {
                        "disable" -> {
                            applistType.isVisible = true
                            selectedApps.isVisible = false
                        }
                        "blacklist", "whitelist" -> {
                            applistType.isVisible = true
                            selectedApps.isVisible = true
                        }
                        else -> {
                            applistType.isVisible = true
                            selectedApps.isVisible = false
                            Log.w(TAG, "Unexpected applistType value: ${applistType.value}")
                        }
                    }
                }

                Mode.Proxy -> {
                    dns.isVisible = false
                    ipv6.isVisible = false
                    applistType.isVisible = false
                    selectedApps.isVisible = false
                }
            }

            if (appSettings.getBoolean("byedpi_enable_cmd_settings")) {
                val cmdArgs = byeDpiSettings.getStringNotNull("byedpi_cmd_args", "").split(" ")
                val ipIndex = cmdArgs.indexOfFirst { it == "-i" || it == "--ip" }
                val portIndex = cmdArgs.indexOfFirst { it == "-p" || it == "--port" }

                proxy.isVisible = ipIndex == -1 && portIndex == -1

                byeDpiProxyArgs = ByeDpiProxyCmdPreferences.createFromStorage(byeDpiSettings)
            } else {
                proxy.isVisible = true

                byeDpiProxyArgs = ByeDpiProxyUIPreferences.createFromStorage(byeDpiSettings)
            }
        }
    }

    private val exportSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                val json = settingsManager.exportSettings()

                requireActivity().contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
            }
        }
    }

    private val importSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let {
            requireActivity().contentResolver.openInputStream(it)?.use { inputStream ->
                val json = inputStream.bufferedReader().readText()

                viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
                    Toast.makeText(requireActivity(), "Invalid config", Toast.LENGTH_LONG).show()
                }) {
                    settingsManager.importSettings(json)
                    requireActivity().recreate()
                }
            }
        }
    }

    private fun Long.toReadableDateTime(): String {
        val format = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault())
        return format.format(this)
    }

    companion object {
        val TAG: String = MainSettingsFragment::class.java.simpleName
    }
}
