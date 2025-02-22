package io.github.dovecoteescapee.byedpi.common.system.di

import androidx.annotation.VisibleForTesting

abstract class LazyComponentHolder<Component : DIComponent> : BaseComponentHolder<Component>, ClearedComponentHolder {

    @Volatile
    private var component: Component? = null
    private var componentProvider: () -> Component = { error("${javaClass.simpleName} — component provider not found") }

    override fun get(): Component {
        val component = component ?: synchronized(this) {
            component ?: run {
                try {
                    componentProvider().also { set(it) }
                } finally {
                }
            }
        }
        return component
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    override fun set(component: Component) {
        this.component = component
    }

    override fun clear() {
        this.component = null
    }

    fun set(provider: () -> Component) {
        componentProvider = provider
    }
}
