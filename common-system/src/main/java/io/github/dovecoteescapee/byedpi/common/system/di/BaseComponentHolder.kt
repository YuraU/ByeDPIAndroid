package io.github.dovecoteescapee.byedpi.common.system.di

interface BaseComponentHolder<Component : DIComponent> {

    fun get(): Component

    fun set(component: Component)
}
