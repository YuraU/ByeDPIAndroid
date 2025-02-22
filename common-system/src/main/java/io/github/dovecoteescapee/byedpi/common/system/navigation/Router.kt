package io.github.dovecoteescapee.byedpi.common.system.navigation

interface Router {

    fun navigateTo(screen: Screen, replace: Boolean = false)

    fun goBack()
}
