package io.github.dovecoteescapee.byedpi.feature.connection.ui.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.ContextComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.NavigationComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.system.di.FeatureComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.navigation.Router
import io.github.dovecoteescapee.byedpi.feature.connection.ui.data.ByDpiServiceManager
import io.github.dovecoteescapee.byedpi.feature.connection.ui.data.LogProvider
import io.github.dovecoteescapee.byedpi.feature.connection.ui.data.VpnServiceChecker
import io.github.dovecoteescapee.byedpi.feature.connection.ui.domain.ConnectionManagerUseCase
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.ViewModelProvider
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.connection.ConnectionFragment
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.connection.ConnectionViewModel
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles.ProfilesFragment
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles.ProfilesViewModel
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.service.QuickTileService

internal interface ConnectionComponent : DIComponent {

    fun inject(fragment: ConnectionFragment)
    fun inject(fragment: ProfilesFragment)
    fun inject(service: QuickTileService)
}

internal class ConnectionComponentImpl(
    private val dependencies: ConnectionComponentDependencies = ConnectionComponentDependencies.Impl
) : ConnectionComponent {

    private val vmProvider by lazy {
        object : ViewModelProvider {
            override fun getConnectionViewModel(): ConnectionViewModel {
                return ConnectionViewModel(
                    appSettings = dependencies.appSettings,
                    byeDpiSettings = dependencies.byeDpiSettings,
                    connectionManagerUseCase = ConnectionManagerUseCase(ByDpiServiceManager(dependencies.context)),
                    vpnServiceChecker = VpnServiceChecker(dependencies.context),
                )
            }

            override fun getProfilesViewModel(): ProfilesViewModel {
                return ProfilesViewModel(
                    appSettings = dependencies.appSettings,
                    byeDpiSettings = dependencies.byeDpiSettings,
                )
            }
        }
    }

    override fun inject(fragment: ConnectionFragment) {
        fragment.appSettings = dependencies.appSettings
        fragment.byeDpiSettings = dependencies.byeDpiSettings
        fragment.router = dependencies.router
        fragment.logProvider = LogProvider()
        fragment.vmProvider = vmProvider
    }

    override fun inject(fragment: ProfilesFragment) {
        fragment.vmProvider = vmProvider
        fragment.appSettings = dependencies.appSettings
        fragment.byeDpiSettings = dependencies.byeDpiSettings
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun inject(service: QuickTileService) {
        service.connectionManagerUseCase = ConnectionManagerUseCase(ByDpiServiceManager(dependencies.context))
    }
}

interface ConnectionComponentDependencies {

    val context: Context
    val appSettings: KeyValueStorage<StorageType.AppSettings>
    val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
    val router: Router

    object Impl : ConnectionComponentDependencies {
        override val context: Context
            get() = ContextComponentHolder.get().context()
        override val appSettings: KeyValueStorage<StorageType.AppSettings>
            get() = StorageComponentHolder.get().appSettings()
        override val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
            get() = StorageComponentHolder.get().byeDpiArgSettings()
        override val router: Router
            get() = NavigationComponentHolder.get().router

    }
}

internal object ConnectionComponentHolder : FeatureComponentHolder<ConnectionComponent>() {
    override fun build(): ConnectionComponent {
        return ConnectionComponentImpl()
    }
}
