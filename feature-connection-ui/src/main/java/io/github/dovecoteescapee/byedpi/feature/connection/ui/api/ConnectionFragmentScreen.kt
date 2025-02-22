package io.github.dovecoteescapee.byedpi.feature.connection.ui.api

import androidx.fragment.app.Fragment
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.ConnectionFragment

class ConnectionFragmentScreen : Screen {

    override fun createFragment(): Fragment {
        return ConnectionFragment()
    }

    override fun getTag() = ConnectionFragment.TAG
}