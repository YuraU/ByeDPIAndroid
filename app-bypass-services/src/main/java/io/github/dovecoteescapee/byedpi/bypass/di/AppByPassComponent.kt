package io.github.dovecoteescapee.byedpi.bypass.di

import android.content.Context
import io.github.dovecoteescapee.byedpi.bypass.data.ByPassManager
import io.github.dovecoteescapee.byedpi.bypass.services.ByeDpiProxyService
import io.github.dovecoteescapee.byedpi.bypass.services.ByeDpiVpnService
import io.github.dovecoteescapee.byedpi.bypass.feature.api.ByPassController
import io.github.dovecoteescapee.byedpi.bypass.feature.api.di.ByPassComponentHolder
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.ContextComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.di.ComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent

interface AppByPassComponent : DIComponent {
    fun inject(proxy: ByeDpiProxyService)
    fun inject(vpn: ByeDpiVpnService)
}

internal class AppByPassComponentImpl(
    private val dependencies: AppByPassComponentDependencies = AppByPassComponentDependencies.Impl
) : AppByPassComponent {

    override fun inject(proxy: ByeDpiProxyService) {
        proxy.byPassManager = ByPassManager(
            ctx = dependencies.ctx,
            byPassController = dependencies.byPassController,
            appSettings = dependencies.appSettings,
            byeDpiSettings = dependencies.byeDpiSettings,
        )
    }

    override fun inject(vpn: ByeDpiVpnService) {
        vpn.appSettings = dependencies.appSettings
        vpn.byPassManager = ByPassManager(
            ctx = dependencies.ctx,
            byPassController = dependencies.byPassController,
            appSettings = dependencies.appSettings,
            byeDpiSettings = dependencies.byeDpiSettings,
        )
    }
}

interface AppByPassComponentDependencies {

    val ctx: Context
    val byPassController: ByPassController
    val appSettings: KeyValueStorage<StorageType.AppSettings>
    val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>

    object Impl : AppByPassComponentDependencies {
        override val ctx: Context
            get() = ContextComponentHolder.get().context()
        override val byPassController: ByPassController
            get() = ByPassComponentHolder.get().byPassController()
        override val appSettings: KeyValueStorage<StorageType.AppSettings>
            get() = StorageComponentHolder.get().appSettings()
        override val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
            get() = StorageComponentHolder.get().byeDpiArgSettings()
    }
}

object AppByPassComponentHolder : ComponentHolder<AppByPassComponent>() {
    override fun build(): AppByPassComponent {
        return AppByPassComponentImpl()
    }
}
