package com.github.jcornaz.miop.experimental.collection

import com.github.jcornaz.miop.experimental.transform
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Represent an event which happened in a [Set]
 */
@Suppress("unused")
public sealed class SetEvent<out E>

/** [element] has been added to the set */
public data class SetElementAdded<out E>(val element: E) : SetEvent<E>()

/** [element] has been removed from the set */
public data class SetElementRemoved<out E>(val element: E) : SetEvent<E>()

/** The set has been cleared */
public object SetCleared : SetEvent<Nothing>()

/**
 * Apply the [event] to this map
 */
public operator fun <E> MutableSet<in E>.plusAssign(event: SetEvent<E>) {
    when (event) {
        is SetElementAdded -> add(event.element)
        is SetElementRemoved -> remove(event.element)
        SetCleared -> clear()
    }
}

/**
 * Compute deltas between each received set and emits the corresponding events.
 */
public fun <E> ReceiveChannel<Set<E>>.toSetEvents(initialSet: Set<E> = emptySet()): ReceiveChannel<SetEvent<E>> = transform(DefaultDispatcher) { input, output ->

    val currentSet = initialSet.toHashSet()

    input.consumeEach { newSet ->
        if (newSet.isEmpty() && currentSet.isNotEmpty()) {
            output.send(SetCleared)
            currentSet.clear()
            return@consumeEach
        }

        val toAdd = newSet.toHashSet()

        val iterator = currentSet.iterator()
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (element !in newSet) {
                output.send(SetElementRemoved(element))
                iterator.remove()
            } else {
                toAdd -= element
            }
        }

        toAdd.forEach {
            output.send(SetElementAdded(it))
            currentSet += it
        }
    }
}
