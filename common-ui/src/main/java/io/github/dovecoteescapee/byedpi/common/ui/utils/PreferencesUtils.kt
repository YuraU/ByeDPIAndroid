package io.github.dovecoteescapee.byedpi.common.ui.utils

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

fun <T : Preference> PreferenceFragmentCompat.findPreferenceNotNull(key: CharSequence): T =
    findPreference(key) ?: throw IllegalStateException("Preference $key not found")
