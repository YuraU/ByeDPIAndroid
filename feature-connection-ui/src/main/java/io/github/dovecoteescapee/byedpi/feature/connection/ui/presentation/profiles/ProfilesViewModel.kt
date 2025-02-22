package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.deleteProfile
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfile
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfiles
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProfilesList
import io.github.dovecoteescapee.byedpi.common.storage.utils.renameProfile
import io.github.dovecoteescapee.byedpi.common.storage.utils.setProfile
import io.github.dovecoteescapee.byedpi.common.storage.utils.setProfiles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ProfilesViewModel(
    private val appSettings: KeyValueStorage<StorageType.AppSettings>,
    private val byeDpiSettings: KeyValueStorage<StorageType.ByeDpiArgSettings>,
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _actions: MutableSharedFlow<Actions> = MutableSharedFlow(extraBufferCapacity = 1)
    val actions: Flow<Actions> = _actions.asSharedFlow()


    init {
        viewModelScope.launch {
            val profiles = appSettings.getProfilesList()

            _state.emit(
                State.Data(
                    profiles = profiles
                )
            )
        }
    }

    fun profilesSwapped(profiles: List<String>) {
        viewModelScope.launch {
            appSettings.setProfiles(profiles.joinToString(";"))
        }
    }

    fun onAddProfileClick() {
        viewModelScope.launch {
            _actions.emit(Actions.ShowAddProfileDialog)
        }
    }

    fun onProfileEdited(profileName: String) {
        viewModelScope.launch {
            _actions.emit(Actions.ShowEditProfileDialog(profileName))
        }
    }

    fun onProfileDeleted(profileName: String) {
        viewModelScope.launch {
            val profiles = appSettings.getProfiles()
            if (profiles?.contains(profileName) == true) {
                appSettings.setProfiles(
                    if (profiles.endsWith(profileName)) {
                        profiles.replace(";$profileName", "")
                    } else {
                        profiles.replace("$profileName;", "")
                    }
                )

                if (appSettings.getProfile() == profileName) {
                    appSettings.setProfile(
                        appSettings.getProfilesList()[0]
                    )
                }

                byeDpiSettings.deleteProfile(profileName)
            }
        }
    }

    fun profileAdded(profileName: String) {
        viewModelScope.launch {
            appSettings.setProfiles(
                appSettings.getProfiles() + ";" + profileName
            )
            _state.emit(
                State.Data(
                    profiles = appSettings.getProfilesList()
                )
            )
        }
    }

    fun profileRename(lastName: String, newName: String) {
        viewModelScope.launch {
            if (appSettings.getProfile() == lastName) {
                appSettings.setProfile(newName)
            }

            appSettings.getProfilesList().forEach {
                if (it == lastName) {
                    appSettings.getProfiles()?.replace(lastName, newName)?.let {
                        appSettings.setProfiles(it)
                    }

                    byeDpiSettings.renameProfile(lastName, newName)

                    _state.emit(
                        State.Data(
                            profiles = appSettings.getProfilesList()
                        )
                    )
                }
            }
        }
    }

    sealed interface State {
        data object Empty : State
        class Data(
            val profiles: List<String>
        ) : State
    }

    sealed interface Actions {
        data object ShowAddProfileDialog : Actions
        class ShowEditProfileDialog(
            val profileName: String
        ) : Actions
    }
}
