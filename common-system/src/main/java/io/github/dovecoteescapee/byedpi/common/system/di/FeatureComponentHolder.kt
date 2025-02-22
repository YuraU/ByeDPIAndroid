package io.github.dovecoteescapee.byedpi.common.system.di

import androidx.annotation.VisibleForTesting
import java.lang.ref.WeakReference

abstract class FeatureComponentHolder<Component : DIComponent> : BaseComponentHolder<Component>, ClearedComponentHolder {

    @Volatile
    private var component: WeakReference<Component>? = null

    override fun get(): Component {
        val component = component?.get() ?: synchronized(this) {
            component?.get() ?: run {
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
        this.component = WeakReference(component)
    }

    protected abstract fun build(): Component

    override fun clear() {
        component = null
    }
}
