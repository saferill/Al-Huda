package com.alhuda.app.core.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

fun <T : NavKey> rootRedirectFallback(
    backStack: NavBackStack<T>,
    root: T,
): (T) -> NavEntry<T> =
    { key ->
        NavEntry(key) {
            LaunchedEffect(key) {
                backStack.remove(key)
                if (backStack.isEmpty()) backStack.add(root)
            }
        }
    }
