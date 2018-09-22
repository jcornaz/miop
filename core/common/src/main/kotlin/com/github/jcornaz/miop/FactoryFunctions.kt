package com.github.jcornaz.miop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlin.coroutines.CoroutineContext

private val EmptyReceiveChannel = GlobalScope.produce<Nothing>(Dispatchers.Unconfined) { }

/**
 * Returns a closed empty [ReceiveChannel]
 */
public fun <E> emptyReceiveChannel(): ReceiveChannel<E> = EmptyReceiveChannel

/**
 * Returns a failed [ReceiveChannel]
 */
public fun <E> failedReceiveChannel(error: Throwable): ReceiveChannel<E> = GlobalScope.produce(Dispatchers.Unconfined) { throw error }

/**
 * Returns a [ReceiveChannel] with the given elements in its buffer
 */
public fun <E> receiveChannelOf(vararg values: E): ReceiveChannel<E> = GlobalScope.produce(Dispatchers.Unconfined, values.size) {
    for (value in values) send(value)
}

/**
 * Returns a [ReceiveChannel] which emits all elements of this iterable (in the same order)
 */
public fun <T> Iterable<T>.openSubscription(context: CoroutineContext = Dispatchers.Unconfined, capacity: Int = 0): ReceiveChannel<T> =
        asSequence().openSubscription(context, capacity)

/**
 * Returns a [ReceiveChannel] which emits all elements of this sequence (in the same order)
 */
public fun <T> Sequence<T>.openSubscription(context: CoroutineContext = Dispatchers.Unconfined, capacity: Int = 0): ReceiveChannel<T> =
        GlobalScope.produce(context, capacity = capacity) { forEach { send(it) } }
