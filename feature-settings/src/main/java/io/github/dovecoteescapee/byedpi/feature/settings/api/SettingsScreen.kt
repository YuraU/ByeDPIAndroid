package io.github.dovecoteescapee.byedpi.feature.settings.api

import androidx.fragment.app.Fragment
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import io.github.dovecoteescapee.byedpi.feature.settings.presentation.settings.MainSettingsFragment

class SettingsScreen : Screen {
    override fun createFragment(): Fragment {
        return MainSettingsFragment()
    }

    override fun getTag() = MainSettingsFragment.TAG
}
