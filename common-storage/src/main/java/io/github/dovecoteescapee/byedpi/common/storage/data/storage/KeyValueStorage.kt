package io.github.dovecoteescapee.byedpi.common.storage.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

interface KeyValueStorage<T> where T : StorageType {

    suspend fun warmUpCache()

    suspend fun observe(): Flow<Any>

    suspend fun getBoolean(key: String): Boolean

    suspend fun getString(key: String): String?

    suspend fun getStringNotNull(key: String, default: String): String

    suspend fun getStringSet(key: String): Set<String>

    suspend fun putStringSet(key: String, value: Set<String>)

    suspend fun putBoolean(key: String, value: Boolean)

    suspend fun putString(key: String, value: String)

    suspend fun getAllData(): Map<String, Any?>

    suspend fun clear()
}

sealed interface StorageType {
    data object AppSettings : StorageType
    data object ByeDpiArgSettings : StorageType
}

internal class KeyValueStorageImpl<T>(
    private val store: DataStore<Preferences>
) : KeyValueStorage<T> where T : StorageType {

    private var map: MutableMap<String, Any?>? = null

    override suspend fun warmUpCache() {
        map = getAllData().toMutableMap()
    }

    override suspend fun observe(): Flow<Any> {
        return store.data
    }

    override suspend fun getBoolean(key: String): Boolean {
        val res = getFromMap<Boolean>(key)
        return if (res != null) {
            res.getOrNull()
        } else {
            withTimeoutOrNull(TIME_OUT) {
                store.data.map { preferences ->
                    preferences[booleanPreferencesKey(key)] ?: false
                }.first()
            }.apply { this?.let { putToMap(key, it) } }
        } ?: false
    }

    override suspend fun getString(key: String): String? {
        val res = getFromMap<String>(key)

        return if (res != null) {
            res.getOrNull()
        } else {
            withTimeoutOrNull(TIME_OUT) {
                store.data.map { preferences ->
                    preferences[stringPreferencesKey(key)]
                }.first()
            }.apply { this?.let { putToMap(key, it) } }
        }
    }


    override suspend fun getStringNotNull(key: String, default: String): String {
        return getString(key) ?: default
    }

    override suspend fun getStringSet(key: String): Set<String> {
        val res = getFromMap<Set<String>>(key)

        return if (res != null) {
            res.getOrNull()
        } else {
            withTimeoutOrNull(TIME_OUT) {
                store.data.map { preferences ->
                    preferences[stringSetPreferencesKey(key)] ?: emptySet()
                }.first()
            }.apply { this?.let { putToMap(key, it) } }
        } ?: emptySet()
    }

    override suspend fun putStringSet(key: String, value: Set<String>) {
        store.edit { preferences ->
            preferences[stringSetPreferencesKey(key)] = value
        }
        putToMap(key, value)
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        store.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
        putToMap(key, value)
    }

    override suspend fun putString(key: String, value: String) {
        store.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
        putToMap(key, value)
    }

    override suspend fun getAllData(): Map<String, Any?> {
        return store.data.first().asMap().mapKeys { it.key.name }
    }

    override suspend fun clear() {
        store.edit { it.clear() }
        map?.clear()
    }

    private inline fun <reified T> getFromMap(key: String): Result<T?>? {
        return if (map?.containsKey(key) == true) {
            Result.success(map?.get(key) as? T)
        } else {
            null
        }
    }

    private fun putToMap(key: String, any: Any) {
        map?.put(key, any)
    }

    companion object {
        const val TIME_OUT = 1_000L
    }
}
