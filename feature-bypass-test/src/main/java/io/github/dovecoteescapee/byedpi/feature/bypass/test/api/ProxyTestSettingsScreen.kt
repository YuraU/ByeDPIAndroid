package io.github.dovecoteescapee.byedpi.feature.bypass.test.api

import androidx.fragment.app.Fragment
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.settings.ProxyTestSettingsFragment

class ProxyTestSettingsScreen :Screen {
    override fun createFragment(): Fragment {
        return ProxyTestSettingsFragment()
    }

    override fun getTag() = ProxyTestSettingsFragment.TAG
}
