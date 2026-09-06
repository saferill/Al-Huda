package com.alhuda.app.core.util.storage

import kotlinx.coroutines.flow.StateFlow

interface SimpleJsonDataStore<T> {
    val data: StateFlow<T>
    suspend fun update(transform: (T) -> T)
    suspend fun getStoredJsonString(): String
}
