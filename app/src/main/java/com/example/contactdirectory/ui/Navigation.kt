package com.example.contactdirectory.ui

import androidx.compose.runtime.mutableStateListOf

/** Destinations in the app. */
sealed interface Dest {
    data object Login : Dest
    data object Contacts : Dest
    data class Detail(val id: Long) : Dest
    data class Edit(val id: Long?) : Dest // null = create new
    data object Trash : Dest
    data object Profile : Dest
}

/** Minimal back-stack navigator (no nav library). Backed by Compose state. */
class Navigator(start: Dest) {
    val stack = mutableStateListOf(start)
    val current: Dest get() = stack.last()
    val canPop: Boolean get() = stack.size > 1

    fun push(dest: Dest) {
        stack.add(dest)
    }

    fun replaceAll(dest: Dest) {
        stack.clear()
        stack.add(dest)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
}
