package io.github.dovecoteescapee.byedpi

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.dovecoteescapee.byedpi.bypass.feature.api.di.ByPassComponent
import io.github.dovecoteescapee.byedpi.bypass.feature.api.di.ByPassComponentHolder
import io.github.dovecoteescapee.byedpi.bypass.feature.api.fromKeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.byeDpiProxyArgs
import io.github.dovecoteescapee.byedpi.common.storage.di.StorageComponentHolder
import io.github.dovecoteescapee.byedpi.common.storage.setMode
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfiles
import io.github.dovecoteescapee.byedpi.common.storage.utils.mode
import io.github.dovecoteescapee.byedpi.common.storage.utils.setProfile
import io.github.dovecoteescapee.byedpi.common.storage.utils.setProfiles
import io.github.dovecoteescapee.byedpi.common.system.components.AppInfoComponent
import io.github.dovecoteescapee.byedpi.common.system.components.AppInfoComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.ContextComponent
import io.github.dovecoteescapee.byedpi.common.system.components.ContextComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.components.NavigationComponent
import io.github.dovecoteescapee.byedpi.common.system.components.NavigationComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.ui.fragment.HasDiComponent
import io.github.dovecoteescapee.byedpi.core.ByPassControllerImpl
import io.github.dovecoteescapee.byedpi.navigation.impl.RouterImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ByeDpiApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val appSettings
        get() = StorageComponentHolder.get().appSettings()

    private val byeDpiArgSettings
        get() = StorageComponentHolder.get().byeDpiArgSettings()

    override fun onCreate() {
        super.onCreate()

        if (isMainProcess()) {
            initComponents()
            initState()

            applicationScope.launch {
                appSettings.warmUpCache()
                byeDpiArgSettings.warmUpCache()
            }

            // Todo refactor to SavedStateRegistry, SavedStateRegistryController
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

                private val components: MutableSet<DIComponent> = mutableSetOf()

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    components.clear()
                }

                override fun onActivityStarted(activity: Activity) = Unit

                override fun onActivityResumed(activity: Activity) = Unit

                override fun onActivityPaused(activity: Activity) = Unit

                override fun onActivityStopped(activity: Activity) = Unit

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

                override fun onActivityDestroyed(activity: Activity) {
                    (activity as AppCompatActivity).supportFragmentManager
                        .fragments
                        .forEach { fragment ->
                            if (fragment is HasDiComponent) {
                                components.add(fragment.component)
                            }
                        }
                }
            })
        }
    }

    private fun initComponents() {
        ContextComponentHolder.set { ContextComponent(this) }
        ByPassComponentHolder.set { ByPassComponent(ByPassControllerImpl) }
        NavigationComponentHolder.set { NavigationComponent(RouterImpl()) }
        AppInfoComponentHolder.set {
            AppInfoComponent(
                appId = BuildConfig.APPLICATION_ID,
                telegramLink = "https://t.me/byebyedpi_group",
                sourceCodeLink = "https://github.com/romanvht/ByeDPIAndroid",
                appVersion = BuildConfig.VERSION_NAME,
                byeDpiVersion = "0.16.5",
            )
        }
    }

    private fun initState() {
        applicationScope.launch {
            setMode(appSettings.mode())

            if (appSettings.getProfiles().isNullOrEmpty()) {
                appSettings.setProfiles("Default")
                appSettings.setProfile("Default")
            }

            byeDpiArgSettings.observe()
                .collect {
                    byeDpiProxyArgs = fromKeyValueStorage(byeDpiArgSettings)
                }
        }
    }

    private fun isMainProcess(): Boolean {
        val processName = getProcName()
        return processName == packageName
    }

    private fun getProcName(): String? {
        return try {
            val pid = android.os.Process.myPid()
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            manager?.runningAppProcesses?.find { it.pid == pid }?.processName
        } catch (e: Exception) {
            null
        }
    }
}
