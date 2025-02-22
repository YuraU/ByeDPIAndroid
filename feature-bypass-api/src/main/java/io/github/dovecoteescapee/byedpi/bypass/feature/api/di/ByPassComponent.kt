package io.github.dovecoteescapee.byedpi.bypass.feature.api.di

import io.github.dovecoteescapee.byedpi.bypass.feature.api.ByPassController
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.system.di.LazyComponentHolder

interface ByPassComponent : DIComponent {
    fun byPassController(): ByPassController
}

fun ByPassComponent(byPassController: ByPassController): ByPassComponent =
    object : ByPassComponent {
        override fun byPassController() = byPassController

    }

object ByPassComponentHolder : LazyComponentHolder<ByPassComponent>()
