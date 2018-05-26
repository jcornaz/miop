package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlin.coroutines.experimental.CoroutineContext

private val EmptyReceiveChannel = produce<Nothing>(Unconfined) { }

/**
 * Return a closed empty [ReceiveChannel]
 */
public fun <E> emptyReceiveChannel(): ReceiveChannel<E> = EmptyReceiveChannel

/**
 * Return a [ReceiveChannel] with the given elements in its buffer
 */
public fun <E> receiveChannelOf(vararg values: E): ReceiveChannel<E> = produce(Unconfined, values.size) {
    for (value in values) send(value)
}

/**
 * Return a [ReceiveChannel] which emits all elements of this iterable (in the same order)
 */
fun <T> Iterable<T>.openSubscription(context: CoroutineContext = Unconfined, capacity: Int = 0): ReceiveChannel<T> =
        asSequence().openSubscription(context, capacity)

/**
 * Return a [ReceiveChannel] which emits all elements of this sequence (in the same order)
 */
fun <T> Sequence<T>.openSubscription(context: CoroutineContext = Unconfined, capacity: Int = 0): ReceiveChannel<T> =
        produce(context, capacity = capacity) { forEach { send(it) } }
