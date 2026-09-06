package com.alhuda.app.core.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Instant

fun tickFlow(initialValue: Instant? = Clock.System.now()): Flow<Instant> =
    flow {

        initialValue?.let { emit(it) }

        while (true) {
            val currentTime = Clock.System.now()
            emit(currentTime)
            delay(1000L)
        }
    }
