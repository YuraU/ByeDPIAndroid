package io.github.dovecoteescapee.byedpi.feature.settings.di

import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.AppInfoComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.NavigationComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.data.AppInfo
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.system.di.FeatureComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.navigation.Router
import io.github.dovecoteescapee.byedpi.feature.settings.data.SettingsManager
import io.github.dovecoteescapee.byedpi.feature.settings.presentation.appselection.AppSelectionFragment
import io.github.dovecoteescapee.byedpi.feature.settings.presentation.settings.ByeDpiCommandLineSettingsFragment
import io.github.dovecoteescapee.byedpi.feature.settings.presentation.settings.ByeDpiUISettingsFragment
import io.github.dovecoteescapee.byedpi.feature.settings.presentation.settings.MainSettingsFragment
import io.github.dovecoteescapee.byedpi.common.storage.utils.HistoryUtils

internal interface AppSettingsComponent : DIComponent {

    fun inject(fragment: MainSettingsFragment)
    fun inject(fragment: ByeDpiCommandLineSettingsFragment)
    fun inject(fragment: ByeDpiUISettingsFragment)
    fun inject(fragment: AppSelectionFragment)
}

internal class AppSettingsComponentImpl(
    private val dependencies: AppSettingsComponentDependencies = AppSettingsComponentDependencies.Impl
) : AppSettingsComponent {

    private val settingsManager: SettingsManager by lazy {
        SettingsManager(
            appInfo = dependencies.appInfo,
            historyUtils = HistoryUtils(dependencies.appSettings),
            appSettings = dependencies.appSettings,
            byeDpiSettings = dependencies.byeDpiSettings,
        )
    }

    override fun inject(fragment: MainSettingsFragment) {
        fragment.appInfo = dependencies.appInfo
        fragment.router = dependencies.router
        fragment.appSettings = dependencies.appSettings
        fragment.byeDpiSettings = dependencies.byeDpiSettings
        fragment.settingsManager = settingsManager
    }

    override fun inject(fragment: ByeDpiCommandLineSettingsFragment) {
        fragment.appSettings = dependencies.appSettings
        fragment.byeDpiSettings = dependencies.byeDpiSettings
    }

    override fun inject(fragment: ByeDpiUISettingsFragment) {
        fragment.byeDpiSettings = dependencies.byeDpiSettings
    }

    override fun inject(fragment: AppSelectionFragment) {
        fragment.appSettings = dependencies.appSettings
    }
}

interface AppSettingsComponentDependencies {

    val appInfo: AppInfo
    val router: Router
    val appSettings: KeyValueStorage<StorageType.AppSettings>
    val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>

    object Impl : AppSettingsComponentDependencies {
        override val appInfo: AppInfo
            get() = AppInfoComponentHolder.get().appInfo
        override val router: Router
            get() = NavigationComponentHolder.get().router
        override val appSettings: KeyValueStorage<StorageType.AppSettings>
            get() = StorageComponentHolder.get().appSettings()
        override val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>
            get() = StorageComponentHolder.get().byeDpiArgSettings()
    }
}

internal object AppSettingsComponentHolder : FeatureComponentHolder<AppSettingsComponent>() {
    override fun build(): AppSettingsComponent {
        return AppSettingsComponentImpl()
    }
}
