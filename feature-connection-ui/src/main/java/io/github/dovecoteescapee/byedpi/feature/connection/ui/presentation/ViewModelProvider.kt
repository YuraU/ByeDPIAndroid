package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation

import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.connection.ConnectionViewModel
import io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles.ProfilesViewModel

internal interface ViewModelProvider {
    fun getConnectionViewModel(): ConnectionViewModel
    fun getProfilesViewModel(): ProfilesViewModel
}
