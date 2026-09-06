package com.alhuda.app.core.domain.audio

const val INTRUSIVE_MIN_DURATION_MS = 5_000L

fun isIntrusiveAudio(
    loop: Boolean,
    durationMs: Long?,
): Boolean = loop || durationMs == null || durationMs >= INTRUSIVE_MIN_DURATION_MS
