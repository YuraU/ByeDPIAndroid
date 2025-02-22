package io.github.dovecoteescapee.byedpi.feature.bypass.test.di

import android.content.Context
import io.github.dovecoteescapee.byedpi.bypass.feature.api.ByPassController
import io.github.dovecoteescapee.byedpi.bypass.feature.api.di.ByPassComponentHolder
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.ContextComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.NavigationComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.system.di.FeatureComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.navigation.Router
import io.github.dovecoteescapee.byedpi.feature.bypass.test.data.ByPassTestConfigProvider
import io.github.dovecoteescapee.byedpi.feature.bypass.test.data.LogManager
import io.github.dovecoteescapee.byedpi.feature.bypass.test.data.ProxyManager
import io.github.dovecoteescapee.byedpi.feature.bypass.test.data.ProxyTestResources
import io.github.dovecoteescapee.byedpi.feature.bypass.test.domain.CheckSitesUseCase
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.ByPassTestFragment
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.ByPassTestViewModel
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.ViewModelProvider
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.settings.ProxyTestSettingsFragment

internal interface ByPassTestComponent : DIComponent {

    fun inject(fragment: ByPassTestFragment)
    fun inject(fragment: ProxyTestSettingsFragment)
}

internal class ByPassTestComponentImpl(
    private val dependencies: ByPassTestComponentDependencies = ByPassTestComponentDependencies.Impl
) : ByPassTestComponent {

    private val byPassTestConfigProvider by lazy {
        ByPassTestConfigProvider(dependencies.context.assets)
    }

    private val vm: ByPassTestViewModel
        get() = ByPassTestViewModel(
            checkSitesUseCase = CheckSitesUseCase(
                logManager = LogManager(dependencies.context),
                appSettings = dependencies.appSettings,
            ),
            proxyTestResources = ProxyTestResources(
                configProvider = byPassTestConfigProvider,
                appSettings = dependencies.appSettings,
            ),
            logManager = LogManager(dependencies.context),
            appSettings = dependencies.appSettings,
            byeDpiSettings = dependencies.byeDpiSettings,
            proxyManager = ProxyManager(dependencies.byPassController)
        )

    private val vmProvider: ViewModelProvider by lazy {
        object : ViewModelProvider {
            override fun getByPassTestViewModel() = vm
        }
    }

    override fun inject(fragment: ByPassTestFragment) {
        fragment.vmProvider = vmProvider
        fragment.router = dependencies.router
    }

    override fun inject(fragment: ProxyTestSettingsFragment) {
        fragment.appSettings = dependencies.appSettings
    }
}

interface ByPassTestComponentDependencies {
    val context: Context
    val byPassController: ByPassController
    val appSettings: KeyValueStorage<StorageType.AppSettings>
    val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
    val router: Router

    object Impl : ByPassTestComponentDependencies {
        override val context: Context
            get() = ContextComponentHolder.get().context()
        override val byPassController: ByPassController
            get() = ByPassComponentHolder.get().byPassController()
        override val appSettings: KeyValueStorage<StorageType.AppSettings>
            get() = StorageComponentHolder.get().appSettings()
        override val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
            get() = StorageComponentHolder.get().byeDpiArgSettings()
        override val router: Router
            get() = NavigationComponentHolder.get().router
    }
}

internal object ByPassTestComponentHolder : FeatureComponentHolder<ByPassTestComponent>() {
    override fun build(): ByPassTestComponent {
        return ByPassTestComponentImpl()
    }
}
