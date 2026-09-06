package com.alhuda.app.core.util.storage

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class OptimisticCommitUpdater<T>(
    private val state: MutableStateFlow<T>,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val commit: suspend (newValue: T) -> Unit,
) {
    private val writeQueue = Channel<T>(capacity = Channel.CONFLATED)

    init {
        scope.launch {
            for (value in writeQueue) {
                try {
                    commit(value)
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t

                }
            }
        }
    }

    fun update(
        transform: (T) -> T,
    ) {
        state.update(transform)
        writeQueue.trySend(state.value)
    }
}
