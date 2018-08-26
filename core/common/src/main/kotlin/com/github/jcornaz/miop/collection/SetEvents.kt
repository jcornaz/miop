package com.github.jcornaz.miop.collection

import com.github.jcornaz.miop.transform
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.coroutineContext

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
public fun <E> ReceiveChannel<Set<E>>.toSetEvents(initialSet: Set<E> = emptySet()): ReceiveChannel<SetEvent<E>> = transform { input, output ->

    val currentSet: MutableSet<E> = initialSet.toHashSet()

    input.consumeEach { newSet ->
        handleNewSet(currentSet, newSet, coroutineContext[Job]).consumeEach { output.send(it) }
    }
}

private fun <E> handleNewSet(currentSet: MutableSet<E>, newSet: Set<E>, parent: Job?): ReceiveChannel<SetEvent<E>> = produce(parent = parent, capacity = Channel.UNLIMITED) {
    if (newSet.isEmpty() && currentSet.isNotEmpty()) {
        send(SetCleared)
        currentSet.clear()
        return@produce
    }

    val toAdd: MutableSet<E> = newSet.toHashSet()

    val iterator = currentSet.iterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (element !in newSet) {
            send(SetElementRemoved(element))
            iterator.remove()
        } else {
            toAdd -= element
        }
    }

    toAdd.forEach {
        send(SetElementAdded(it))
        currentSet += it
    }
}
