package io.github.dovecoteescapee.byedpi.common.system.navigation

import androidx.fragment.app.Fragment

interface Screen {
    fun createFragment(): Fragment
    fun getTag(): String
}
