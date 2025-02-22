package io.github.dovecoteescapee.byedpi.common.system.di

import androidx.annotation.VisibleForTesting

abstract class ComponentHolder<Component : DIComponent> : BaseComponentHolder<Component>, ClearedComponentHolder {

    @Volatile
    private var component: Component? = null

    override fun get(): Component {
        val component = component ?: synchronized(this) {
            component ?: run {
                try {
                    build().also { set(it) }
                } finally {
                }
            }
        }
        return component
    }

    @VisibleForTesting
    override fun set(component: Component) {
        this.component = component
    }

    protected abstract fun build(): Component

    override fun clear() {
        component = null
    }
}
