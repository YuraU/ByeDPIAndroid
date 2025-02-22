package io.github.dovecoteescapee.byedpi.common.system.components

import io.github.dovecoteescapee.byedpi.common.system.data.AppInfo
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.system.di.LazyComponentHolder

interface AppInfoComponent : DIComponent {
    val appInfo: AppInfo
}

fun AppInfoComponent(
    appId: String,
    telegramLink: String,
    sourceCodeLink: String,
    appVersion: String,
    byeDpiVersion: String
): AppInfoComponent =
    AppInfoComponentImpl(
        object : AppInfoDependencies {
            override val appInfo: AppInfo
                get() = AppInfo(
                    appId = appId,
                    telegramLink = telegramLink,
                    sourceCodeLink = sourceCodeLink,
                    appVersion = appVersion,
                    byeDpiVersion = byeDpiVersion
                )
        }
    )

internal interface AppInfoDependencies {
    val appInfo: AppInfo
}

internal class AppInfoComponentImpl(
    dependencies: AppInfoDependencies,
) : AppInfoComponent, AppInfoDependencies by dependencies

object AppInfoComponentHolder : LazyComponentHolder<AppInfoComponent>()
