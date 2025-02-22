package io.github.dovecoteescapee.byedpi.common.system.components

import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.system.di.LazyComponentHolder
import io.github.dovecoteescapee.byedpi.common.system.navigation.Router

interface NavigationComponent : DIComponent {
    val router: Router
}

fun NavigationComponent(router: Router): NavigationComponent =
    object : NavigationComponent {
        override val router: Router
            get() = router

    }

object NavigationComponentHolder : LazyComponentHolder<NavigationComponent>()
