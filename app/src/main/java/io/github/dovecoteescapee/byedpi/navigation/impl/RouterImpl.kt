package io.github.dovecoteescapee.byedpi.navigation.impl

import io.github.dovecoteescapee.byedpi.common.system.navigation.Router
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import java.util.Stack

class RouterImpl : Router {
    private var navigator: Navigator? = null
    private val backStack = Stack<Screen>()

    override fun navigateTo(screen: Screen, replace: Boolean) {
        backStack.push(screen)
        navigator?.showScreen(screen, replace)
        navigator?.updateActionBar(backStack.size)
    }

    override fun goBack() {
        if (backStack.size > 1) {
            backStack.pop()
            navigator?.goBack()
            navigator?.updateActionBar(backStack.size)
        } else {
            navigator?.exit()
        }
    }

    fun setNavigator(navigator: Navigator, clearBackStack: Boolean) {
        this.navigator = navigator
        if (clearBackStack) {
            backStack.clear()
        } else {
            navigator.updateActionBar(backStack.size)
        }
    }

    fun clearNavigator() {
        this.navigator = null
    }
}
