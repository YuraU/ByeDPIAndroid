package io.github.dovecoteescapee.byedpi.feature.settings.api

import androidx.fragment.app.Fragment
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import io.github.dovecoteescapee.byedpi.feature.settings.presentation.settings.ByeDpiUISettingsFragment

class ByeDpiUISettingsScreen : Screen {
    override fun createFragment(): Fragment {
        return ByeDpiUISettingsFragment()
    }

    override fun getTag() = ByeDpiUISettingsFragment.TAG
}
