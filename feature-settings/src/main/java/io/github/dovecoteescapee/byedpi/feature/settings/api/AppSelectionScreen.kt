package io.github.dovecoteescapee.byedpi.feature.settings.api

import androidx.fragment.app.Fragment
import io.github.dovecoteescapee.byedpi.common.system.navigation.Screen
import io.github.dovecoteescapee.byedpi.feature.settings.presentation.appselection.AppSelectionFragment

class AppSelectionScreen : Screen {

    override fun createFragment(): Fragment {
        return AppSelectionFragment()
    }

    override fun getTag() = AppSelectionFragment.TAG
}
