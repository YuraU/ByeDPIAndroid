package io.github.dovecoteescapee.byedpi.feature.settings.data

import com.google.gson.Gson
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.system.data.AppInfo
import io.github.dovecoteescapee.byedpi.common.storage.utils.HistoryUtils
import io.github.dovecoteescapee.byedpi.common.storage.utils.getSelectedApps
import io.github.dovecoteescapee.byedpi.common.storage.utils.putSelectedApps

class SettingsManager(
    private val appInfo: AppInfo,
    private val historyUtils: HistoryUtils,
    private val appSettings: KeyValueStorage<StorageType.AppSettings>,
    private val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>,
) {

    suspend fun exportSettings(): String {
        val history = historyUtils.getHistory()
        val apps = appSettings.getSelectedApps().toList()

        val globalSettings = appSettings.getAllData().filterKeys { key ->
            key !in setOf("byedpi_command_history", "selected_apps")
        }

        val _byeDpiSettings = byeDpiSettings.getAllData()

        val export = AppSettings(
            app = appInfo.appId,
            version = appInfo.appVersion,
            history = history,
            apps = apps,
            globalSettings = globalSettings,
            byeDpiSettings = _byeDpiSettings,
        )

        val json = Gson().toJson(export)

        return json
    }

    suspend fun importSettings(json: String) {
        val appSettingsFromJson = Gson().fromJson(json, AppSettings::class.java)

        appSettings.clear()
        byeDpiSettings.clear()

        if (appSettingsFromJson.app != appInfo.appId) {
            error("Invalid config")
        }

        appSettingsFromJson.globalSettings.forEach { (key, value) ->
            when (value) {
                is Boolean -> appSettings.putBoolean(key, value)
                is String -> appSettings.putString(key, value)
            }
        }
        appSettings.putSelectedApps(appSettingsFromJson.apps.toSet())
        historyUtils.saveHistory(appSettingsFromJson.history)

        appSettingsFromJson.byeDpiSettings.forEach { (key, value) ->
            when (value) {
                is Boolean -> byeDpiSettings.putBoolean(key, value)
                is String -> byeDpiSettings.putString(key, value)
            }
        }
    }
}
