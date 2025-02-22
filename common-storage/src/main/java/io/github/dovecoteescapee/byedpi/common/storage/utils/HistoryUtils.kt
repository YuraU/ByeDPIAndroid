package io.github.dovecoteescapee.byedpi.common.storage.utils

import com.google.gson.Gson
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.KeyValueStorage
import io.github.dovecoteescapee.byedpi.common.storage.data.storage.StorageType

class HistoryUtils(
    private val storage: KeyValueStorage<StorageType.AppSettings>
) {
    private val maxHistorySize = 40

    suspend fun addCommand(command: String) {
        if (command.isBlank()) return

        val history = getHistory().toMutableList()
        val search = history.find { it.text == command }

        if (search == null) {
            history.add(0, Command(command))
            if (history.size > maxHistorySize) {
                history.removeAt(maxHistorySize)
            }
        }

        saveHistory(history)
    }

    suspend fun pinCommand(command: String) {
        val history = getHistory().toMutableList()
        history.find { it.text == command }?.pinned = true
        saveHistory(history)
    }

    suspend fun unpinCommand(command: String) {
        val history = getHistory().toMutableList()
        history.find { it.text == command }?.pinned = false
        saveHistory(history)
    }

    suspend fun deleteCommand(command: String) {
        val history = getHistory().toMutableList()
        history.removeAll { it.text == command }
        saveHistory(history)
    }

    suspend fun renameCommand(command: String, newName: String) {
        val history = getHistory().toMutableList()
        history.find { it.text == command }?.name = newName
        saveHistory(history)
    }

    suspend fun getHistory(): List<Command> {
        val historyJson = storage.getHistory()
        return if (historyJson != null) {
            Gson().fromJson(historyJson, Array<Command>::class.java).toList()
        } else {
            emptyList()
        }
    }

    suspend fun saveHistory(history: List<Command>) {
        val historyJson = Gson().toJson(history)
        storage.putHistory(historyJson)
    }
}

data class Command(
    val text: String,
    var pinned: Boolean = false,
    var name: String? = null
)
