package io.github.dovecoteescapee.byedpi.common.storage.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.ByeDpiStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorageImpl
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.system.components.ContextComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.di.ComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent

interface StorageComponent : DIComponent {
    fun appSettings(): KeyValueStorage<StorageType.AppSettings>
    fun byeDpiArgSettings(): KeyValueStorage<StorageType.ByeDpiArgSettings>
}

internal class StorageComponentImpl(
    private val context: Context = ContextComponentHolder.get().context()
) : StorageComponent {

    private val Context.appDataStore by preferencesDataStore(name = "settings_prefs")
    private val Context.byeDpiDataStore by preferencesDataStore(name = "bye_dpi_prefs")

    private val appStorage by lazy {
        KeyValueStorageImpl<StorageType.AppSettings>(
            store = context.appDataStore
        )
    }

    private val byeDpiArgStorage by lazy {
        ByeDpiStorage(
            appStorage = appStorage,
            delegate = KeyValueStorageImpl(
                store = context.byeDpiDataStore
            )
        )
    }

    override fun appSettings() = appStorage

    override fun byeDpiArgSettings() = byeDpiArgStorage
}

object StorageComponentHolder : ComponentHolder<StorageComponent>() {
    override fun build(): StorageComponent = StorageComponentImpl()
}
