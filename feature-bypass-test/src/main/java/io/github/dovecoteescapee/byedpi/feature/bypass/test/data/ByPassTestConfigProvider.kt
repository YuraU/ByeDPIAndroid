package io.github.dovecoteescapee.byedpi.feature.bypass.test.data

import android.content.res.AssetManager

class ByPassTestConfigProvider(
    private val assets: AssetManager
) {

    fun getSites(): List<String> {
        return assets.open("proxytest_sites.txt").bufferedReader().useLines { it.toList() }
    }

    fun getCommands(): List<String> {
        return assets.open("proxytest_cmds.txt").bufferedReader().useLines { it.toList() }
    }
}
