package com.example.polyquiz

import StringValue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

// Reference: https://youtu.be/KFazs62lIkE
data class SnackbarEvent(
    val message: StringValue,
    val action: SnackbarAction? = null
)

data class SnackbarAction(
    val name: String,
    val action: () -> Unit
)

object SnackbarController {
    private val _events = Channel<SnackbarEvent> {  }
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: SnackbarEvent) {
        _events.send(event)
    }
}
