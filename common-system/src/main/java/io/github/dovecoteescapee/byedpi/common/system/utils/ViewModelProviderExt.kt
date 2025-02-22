package io.github.dovecoteescapee.byedpi.common.system.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified VM : ViewModel> Fragment.savedStateViewModel(
    crossinline factory: (handle: SavedStateHandle) -> VM
): Lazy<VM> = lazyUnsafe {
    val fragment = this
    ViewModelProvider(
        fragment,
        object : AbstractSavedStateViewModelFactory(fragment, fragment.arguments) {
            override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T = factory(handle) as T
        }
    )[VM::class.java]
}

fun <T> lazyUnsafe(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)
