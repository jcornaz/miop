package com.github.jcornaz.miop.collection

import com.github.jcornaz.miop.transform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

/**
 * Represent an event which happened in a [Set]
 */
@Suppress("unused")
@ExperimentalCollectionEvent
public sealed class SetEvent<out E>

/** [element] has been added to the set */
@ExperimentalCollectionEvent
public data class SetElementAdded<out E>(val element: E) : SetEvent<E>()

/** [element] has been removed from the set */
@ExperimentalCollectionEvent
public data class SetElementRemoved<out E>(val element: E) : SetEvent<E>()

/** The set has been cleared */
@ExperimentalCollectionEvent
public object SetCleared : SetEvent<Nothing>()

/**
 * Apply the [event] to this map
 */
@ExperimentalCollectionEvent
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
@ExperimentalCollectionEvent
public fun <E> ReceiveChannel<Set<E>>.toSetEvents(initialSet: Set<E> = emptySet()): ReceiveChannel<SetEvent<E>> = transform { input, output ->

    val currentSet: MutableSet<E> = initialSet.toHashSet()

    input.consumeEach { newSet ->
        handleNewSet(currentSet, newSet).consumeEach { output.send(it) }
    }
}

@ExperimentalCollectionEvent
private fun <E> CoroutineScope.handleNewSet(currentSet: MutableSet<E>, newSet: Set<E>): ReceiveChannel<SetEvent<E>> = produce(Dispatchers.Default, Channel.UNLIMITED) {
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
