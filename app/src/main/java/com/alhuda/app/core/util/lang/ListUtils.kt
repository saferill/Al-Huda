package com.alhuda.app.core.util.lang

object ListUtils {
    fun <T> replaceInImmutableList(
        list: List<T>,
        index: Int,
        newItem: T,
    ): List<T> =
        list.mapIndexed { i, item ->
            if (i == index) newItem else item
        }
}
