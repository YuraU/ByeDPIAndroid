package io.github.dovecoteescapee.byedpi.feature.connection.ui.data

import android.content.Intent
import android.util.Log

class LogProvider {

    fun collectLogs(): String? {
        return try {
            Runtime.getRuntime()
                .exec("logcat *:D -d")
                .inputStream.bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to collect logs", e)
            null
        }
    }

    fun createIntent(): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, LOG_FILE_NAME)
        }
    }

    private companion object {
        val TAG = LogProvider::class.java.simpleName
        const val LOG_FILE_NAME ="byedpi.log"
    }
}
