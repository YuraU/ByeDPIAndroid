package io.github.dovecoteescapee.byedpi.common.system.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

inline fun <T> Flow<T>.observeFlowWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        this@observeFlowWithLifecycle.flowWithLifecycle(lifecycle = lifecycleOwner.lifecycle, minActiveState = minActiveState).collect { block(it) }
    }
}
