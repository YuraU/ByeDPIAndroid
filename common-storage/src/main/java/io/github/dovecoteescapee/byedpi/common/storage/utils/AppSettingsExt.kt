package io.github.dovecoteescapee.byedpi.common.storage.utils

import io.github.dovecoteescapee.byedpi.common.storage.Mode
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType

suspend fun KeyValueStorage<StorageType.AppSettings>.mode(): Mode =
    Mode.fromString(getStringNotNull("byedpi_mode", "vpn"))

suspend fun KeyValueStorage<StorageType.AppSettings>.getSelectedApps(): List<String> {
    return getStringSet("selected_apps").toList()
}

suspend fun KeyValueStorage<StorageType.AppSettings>.putSelectedApps(settings: Set<String>) {
    putStringSet("selected_apps", settings)
}

suspend fun KeyValueStorage<StorageType.AppSettings>.getAppListType(): String {
    return getStringNotNull("applist_type", "disable")
}

suspend fun KeyValueStorage<StorageType.AppSettings>.getHistory(): String? {
    return getString("byedpi_command_history")
}

suspend fun KeyValueStorage<StorageType.AppSettings>.putHistory(history: String) {
    return putString("byedpi_command_history", history)
}

suspend fun KeyValueStorage<StorageType.AppSettings>.getDns(): String {
    return getStringNotNull("dns_ip", "8.8.8.8")
}

suspend fun KeyValueStorage<StorageType.AppSettings>.isIpV6Enabled(): Boolean {
    return getBoolean("ipv6_enable")
}

suspend fun KeyValueStorage<StorageType.AppSettings>.getProfile(): String? {
    return getString("profile")
}

suspend fun KeyValueStorage<StorageType.AppSettings>.setProfile(profile: String) {
    return putString("profile", profile)
}


suspend fun KeyValueStorage<StorageType.AppSettings>.getProfilesList(): List<String> {
    return getProfiles()?.split(";") ?: emptyList()
}

suspend fun KeyValueStorage<StorageType.AppSettings>.getProfiles(): String? {
    return getString("profiles")
}

suspend fun KeyValueStorage<StorageType.AppSettings>.setProfiles(profiles: String) {
    putString("profiles", profiles)
}
