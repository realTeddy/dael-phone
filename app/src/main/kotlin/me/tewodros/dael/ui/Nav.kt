package me.tewodros.dael.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * A deliberately tiny back stack. The navigation library's animated, cancellable back handling
 * could be driven into a blank screen by a toddler mashing Back; this cannot. The stack never
 * drops below one entry, pops are idempotent, and there are no transitions to interrupt.
 */
class Nav(start: String) {
    val stack: SnapshotStateList<String> = mutableStateListOf(start)
    val current: String get() = stack.last()

    fun navigate(route: String) {
        if (stack.last() != route) stack.add(route)
    }

    /** Pops to [route] if present (keeping it), then pushes [to]. */
    fun navigate(to: String, popUpTo: String) {
        val idx = stack.indexOf(popUpTo)
        if (idx >= 0) while (stack.size > idx + 1) stack.removeAt(stack.size - 1)
        navigate(to)
    }

    fun back() {
        if (stack.size > 1) stack.removeAt(stack.size - 1)
    }

    fun home() {
        while (stack.size > 1) stack.removeAt(stack.size - 1)
    }
}
