package io.github.dovecoteescapee.byedpi.feature.settings.api

import androidx.fragment.app.Fragment
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import io.github.dovecoteescapee.byedpi.feature.settings.presentation.settings.ByeDpiCommandLineSettingsFragment

class ByeDpiCommandLineSettingsScreen : Screen {
    override fun createFragment(): Fragment {
        return ByeDpiCommandLineSettingsFragment()
    }

    override fun getTag() = ByeDpiCommandLineSettingsFragment.TAG
}
