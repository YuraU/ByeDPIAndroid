package io.github.dovecoteescapee.byedpi.feature.bypass.test.data

import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType

class ProxyTestResources(
    private val configProvider: ByPassTestConfigProvider,
    private val appSettings: KeyValueStorage<StorageType.AppSettings>,
) {

    suspend fun loadSites(): List<String> {
        val userDomains = appSettings.getBoolean("byedpi_proxytest_userdomains")
        return if (userDomains) {
            val domains = appSettings.getString("byedpi_proxytest_domains").orEmpty()
            domains.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } else {
            configProvider.getSites()
        }
    }

    suspend fun loadCmds(): List<String> {
        val userCommands = appSettings.getBoolean("byedpi_proxytest_usercommands")
        return if (userCommands) {
            val commands = appSettings.getString("byedpi_proxytest_commands").orEmpty()
            commands.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } else {
            configProvider.getCommands()
        }
    }
}
