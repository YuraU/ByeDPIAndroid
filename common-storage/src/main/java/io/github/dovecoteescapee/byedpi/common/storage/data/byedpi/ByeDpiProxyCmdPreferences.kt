package io.github.dovecoteescapee.byedpi.common.storage.data.byedpi

import android.util.Log
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType
import io.github.dovecoteescapee.byedpi.common.storage.utils.getCmdArgs
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProxyIp
import io.github.dovecoteescapee.byedpi.common.storage.utils.getProxyPort

class ByeDpiProxyCmdPreferences(
    override val args: Array<String>
) : ByeDpiProxyArgs {

    companion object {
        suspend fun createFromStorage(storage: KeyValueStorage<StorageType.ByeDpiArgSettings>): ByeDpiProxyCmdPreferences {
            val cmd = storage.getCmdArgs().orEmpty()
            val firstArgIndex = cmd.indexOf("-")
            val args = (if (firstArgIndex > 0) cmd.substring(firstArgIndex) else cmd).trim()

            Log.d("ProxyPref", "CMD: $args")

            val hasIp = args.contains("-i ") || args.contains("--ip ")
            val hasPort = args.contains("-p ") || args.contains("--port ")

            val ip = storage.getProxyIp()
            val port = storage.getProxyPort()

            val prefix = buildString {
                if (!hasIp) append("-i $ip ")
                if (!hasPort) append("-p $port ")
            }

            Log.d("ProxyPref", "Added from settings: $prefix")

            if (prefix.isNotEmpty()) {
                return ByeDpiProxyCmdPreferences(
                    arrayOf("ciadpi") + shellSplit("$prefix$args")
                )
            }

            return ByeDpiProxyCmdPreferences(arrayOf("ciadpi") + shellSplit(args))
        }

        fun createFromCmd(cmd: String): ByeDpiProxyCmdPreferences {
            val firstArgIndex = cmd.indexOf("-")
            val args = (if (firstArgIndex > 0) cmd.substring(firstArgIndex) else cmd).trim()

            Log.d("ProxyPref", "CMD: $args")

            return ByeDpiProxyCmdPreferences(arrayOf("ciadpi") + shellSplit(args))
        }

        // Based on https://gist.github.com/raymyers/8077031
        private fun shellSplit(string: CharSequence): List<String> {
            val tokens: MutableList<String> = ArrayList()
            var escaping = false
            var quoteChar = ' '
            var quoting = false
            var lastCloseQuoteIndex = Int.MIN_VALUE
            var current = StringBuilder()

            for (i in string.indices) {
                val c = string[i]

                if (escaping) {
                    current.append(c)
                    escaping = false
                } else if (c == '\\' && !(quoting && quoteChar == '\'')) {
                    escaping = true
                } else if (quoting && c == quoteChar) {
                    quoting = false
                    lastCloseQuoteIndex = i
                } else if (!quoting && (c == '\'' || c == '"')) {
                    quoting = true
                    quoteChar = c
                } else if (!quoting && Character.isWhitespace(c)) {
                    if (current.isNotEmpty() || lastCloseQuoteIndex == i - 1) {
                        tokens.add(current.toString())
                        current = StringBuilder()
                    }
                } else {
                    current.append(c)
                }
            }

            if (current.isNotEmpty() || lastCloseQuoteIndex == string.length - 1) {
                tokens.add(current.toString())
            }

            return tokens
        }
    }
}
