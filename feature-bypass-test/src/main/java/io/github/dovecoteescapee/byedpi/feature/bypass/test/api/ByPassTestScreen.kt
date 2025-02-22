package io.github.dovecoteescapee.byedpi.feature.bypass.test.api

import androidx.fragment.app.Fragment
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.ByPassTestFragment

class ByPassTestScreen : Screen {
    override fun createFragment(): Fragment {
        return ByPassTestFragment()
    }

    override fun getTag() = ByPassTestFragment.TAG
}
