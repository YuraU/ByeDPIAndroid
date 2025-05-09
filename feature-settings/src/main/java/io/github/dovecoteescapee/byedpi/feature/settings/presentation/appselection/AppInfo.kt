package io.github.dovecoteescapee.byedpi.feature.settings.presentation.appselection

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    var isSelected: Boolean
)
