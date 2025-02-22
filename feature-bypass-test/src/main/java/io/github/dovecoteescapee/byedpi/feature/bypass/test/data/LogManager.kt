package io.github.dovecoteescapee.byedpi.feature.bypass.test.data

import android.content.Context
import io.github.dovecoteescapee.byedpi.feature.bypass.test.presentation.ByPassTestViewModel.State.Data.CommandType
import java.io.File

internal class LogManager(
    private val ctx: Context,
) {

    fun saveLog(text: String) {
        val file = File(ctx.filesDir, FILE_LOG_NAME)
        file.appendText(text + "\n")
    }

    fun clearLog() {
        val file = File(ctx.filesDir, FILE_LOG_NAME)
        file.writeText("")
    }

    fun loadLog(): String {
        val file = File(ctx.filesDir, FILE_LOG_NAME)
        return if (file.exists()) file.readText() else ""
    }

    fun parseLog(log: String): List<CommandType> {
        val lines: List<String> = log.split("\n")

        var cmd = ""
        var results: String

        val list = mutableListOf<CommandType>()

        lines.forEach { line ->
            if (line.startsWith("cmd:")) {
                cmd = line
                    .replace("cmd:", "")
                    .replace("{", "")
                    .replace("}", "")
            } else {
                results = line
                    .replace("{", "")
                    .replace("}", "")

                list.add(
                    CommandType(
                        cmd = cmd,
                        results = results
                    )
                )
            }
        }

        return list
    }

    private companion object {
        const val FILE_LOG_NAME = "proxy_test.log"
    }
}
