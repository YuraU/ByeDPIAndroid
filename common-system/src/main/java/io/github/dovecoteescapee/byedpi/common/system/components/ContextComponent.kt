package io.github.dovecoteescapee.byedpi.common.system.components

import android.content.Context
import io.github.dovecoteescapee.byedpi.common.system.di.DIComponent
import io.github.dovecoteescapee.byedpi.common.system.di.LazyComponentHolder

fun interface ContextComponent : DIComponent {
    fun context(): Context
}

fun ContextComponent(context: Context): ContextComponent =
    ContextComponentImpl(
        object : ContextDependencies {
            override val context: Context
                get() = context
        }
    )

internal interface ContextDependencies {
    val context: Context
}

internal class ContextComponentImpl(
    dependencies: ContextDependencies,
) : ContextComponent, ContextDependencies by dependencies {

    override fun context(): Context = context
}

object ContextComponentHolder : LazyComponentHolder<ContextComponent>()
