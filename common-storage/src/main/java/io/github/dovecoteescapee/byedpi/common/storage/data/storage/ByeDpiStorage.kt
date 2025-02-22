package io.github.dovecoteescapee.byedpi.common.storage.data.storage

import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfile

class ByeDpiStorage(
    private val appStorage: KeyValueStorage<StorageType.AppSettings>,
    private val delegate: KeyValueStorage<StorageType.ByeDpiArgSettings>
) : KeyValueStorage<StorageType.ByeDpiArgSettings> by delegate {

    override suspend fun getBoolean(key: String): Boolean {
        return delegate.getBoolean(replaceKeyIfNeed(key))
    }

    override suspend fun getString(key: String): String? {
        return delegate.getString(replaceKeyIfNeed(key))
    }

    override suspend fun getStringNotNull(key: String, default: String): String {
        return delegate.getStringNotNull(replaceKeyIfNeed(key), default)
    }

    override suspend fun getStringSet(key: String): Set<String> {
        return delegate.getStringSet(replaceKeyIfNeed(key))
    }

    override suspend fun putStringSet(key: String, value: Set<String>) {
        delegate.putStringSet(replaceKeyIfNeed(key), value)
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        delegate.putBoolean(replaceKeyIfNeed(key), value)
    }

    override suspend fun putString(key: String, value: String) {
        delegate.putString(replaceKeyIfNeed(key), value)
    }

    private suspend fun replaceKeyIfNeed(key: String): String {
        return if (key.contains("{") && key.contains("}")) {
            key
        } else {
            "{${appStorage.getProfile()}}_$key"
        }
    }
}
