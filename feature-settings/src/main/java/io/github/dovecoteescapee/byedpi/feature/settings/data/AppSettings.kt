package io.github.dovecoteescapee.byedpi.feature.settings.data

import io.github.dovecoteescapee.byedpi.common.storage.utils.Command

data class AppSettings(
    val app: String,
    val version: String,
    val history: List<Command>,
    val apps: List<String>,
    val globalSettings: Map<String, Any?>,
    val byeDpiSettings: Map<String, Any?>,
)
