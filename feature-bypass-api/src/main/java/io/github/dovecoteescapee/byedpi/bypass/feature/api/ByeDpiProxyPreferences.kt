package io.github.dovecoteescapee.byedpi.bypass.feature.api

import io.github.dovecoteescapee.byedpi.common.storage.data.byedpi.ByeDpiProxyArgs
import io.github.dovecoteescapee.byedpi.common.storage.data.byedpi.ByeDpiProxyCmdPreferences
import io.github.dovecoteescapee.byedpi.common.storage.data.byedpi.ByeDpiProxyUIPreferences
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.isCmdEnable

suspend fun fromKeyValueStorage(
    byeDpiArgSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
): ByeDpiProxyArgs =
    when (byeDpiArgSettings.isCmdEnable()) {
        true -> ByeDpiProxyCmdPreferences.createFromStorage(byeDpiArgSettings)
        false -> ByeDpiProxyUIPreferences.createFromStorage(byeDpiArgSettings)
    }
