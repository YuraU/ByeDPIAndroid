package io.github.dovecoteescapee.byedpi.common.storage.utils

import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType

suspend fun KeyValueStorage<StorageType.ByeDpiArgSettings>.isCmdEnable(): Boolean {
    return getBoolean("byedpi_enable_cmd_settings")
}

suspend fun KeyValueStorage<StorageType.ByeDpiArgSettings>.getCmdArgs(): String? {
    return getString("byedpi_cmd_args")
}

suspend fun KeyValueStorage<StorageType.ByeDpiArgSettings>.putCmdArgs(cmd: String) {
    putString("byedpi_cmd_args", cmd)
}

suspend fun KeyValueStorage<StorageType.ByeDpiArgSettings>.getProxyIp(): String {
    return getStringNotNull("byedpi_proxy_ip", "127.0.0.1")
}

suspend fun KeyValueStorage<StorageType.ByeDpiArgSettings>.getProxyPort(): String {
    return getStringNotNull("byedpi_proxy_port", "1080")
}

suspend fun KeyValueStorage<StorageType.ByeDpiArgSettings>.getProxyIpAndPort(): Pair<String, String> {
    val cmdEnable = isCmdEnable()
    val cmdArgs = if (cmdEnable) getCmdArgs() else null
    val args = cmdArgs?.split(" ") ?: emptyList()

    fun getArgValue(argsList: List<String>, keys: List<String>): String? {
        for (key in keys) {
            val index = argsList.indexOf(key)
            if (index != -1 && index + 1 < argsList.size) {
                return argsList[index + 1]
            }
        }
        return null
    }

    val cmdIp = getArgValue(args, listOf("-i", "--ip"))
    val cmdPort = getArgValue(args, listOf("-p", "--port"))

    val ip = cmdIp ?: getProxyIp()
    val port = cmdPort ?: getProxyPort()

    return Pair(ip, port)
}

suspend fun KeyValueStorage<StorageType.ByeDpiArgSettings>.deleteProfile(profile: String) {
    val data = getAllData()
    clear()

    data.keys.forEach { key ->
        if (!key.contains("{${profile}}_")) {
            when (data[key]) {
                is Boolean -> putBoolean(key, data[key] as Boolean)
                is String -> putString(key, data[key] as String)
            }
        }
    }
}

suspend fun KeyValueStorage<StorageType.ByeDpiArgSettings>.renameProfile(profile: String, name: String) {
    val data = getAllData()
    clear()

    data.keys.forEach { key ->
        val newKey = if (key.contains("{${profile}}_")) {
            key.replace("{${profile}}_", "{${name}}_")
        } else {
            key
        }

        when (data[key]) {
            is Boolean -> putBoolean(newKey, data[key] as Boolean)
            is String -> putString(newKey, data[key] as String)
        }
    }
}
