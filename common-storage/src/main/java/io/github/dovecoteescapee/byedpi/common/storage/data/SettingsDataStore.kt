package io.github.dovecoteescapee.byedpi.common.storage.data

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference
import androidx.preference.PreferenceDataStore
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType

class SettingsDataStore(
    private val scope: WeakReference<LifecycleCoroutineScope>,
    private val storage: KeyValueStorage<StorageType>,
    private val dataStoreChangeListener: MutableSharedFlow<String>? = null
) : PreferenceDataStore() {

    override fun putString(key: String, value: String?) {
        scope.get()?.launch {
            storage.putString(key, value.orEmpty())
            dataStoreChangeListener?.tryEmit(key)
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        scope.get()?.launch {
            storage.putBoolean(key, value)
            dataStoreChangeListener?.tryEmit(key)
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return runBlocking {
            withTimeoutOrNull(TIME_OUT) {
                storage.getString(key)
            } ?: defValue
        }
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return runBlocking {
            withTimeoutOrNull(TIME_OUT) {
                storage.getStringSet(key)
            } ?: defValues
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return runBlocking {
            withTimeoutOrNull(TIME_OUT) {
                storage.getBoolean(key)
            } ?: defValue
        }
    }

    companion object {
        const val TIME_OUT = 500L
    }
}
