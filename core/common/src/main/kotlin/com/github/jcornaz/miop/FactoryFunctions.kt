package com.github.jcornaz.miop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
private val EmptyReceiveChannel = GlobalScope.produce<Nothing>(Dispatchers.Unconfined) { }

/**
 * Returns a closed empty [ReceiveChannel]
 */
@ExperimentalCoroutinesApi
public fun <E> emptyReceiveChannel(): ReceiveChannel<E> = EmptyReceiveChannel

/**
 * Returns a failed [ReceiveChannel]
 */
@ExperimentalCoroutinesApi
public fun <E> failedReceiveChannel(error: Throwable): ReceiveChannel<E> = GlobalScope.produce(Dispatchers.Unconfined) { throw error }

/**
 * Returns a [ReceiveChannel] with the given elements in its buffer
 */
@ExperimentalCoroutinesApi
public fun <E> receiveChannelOf(vararg values: E): ReceiveChannel<E> = GlobalScope.produce(Dispatchers.Unconfined, values.size) {
    for (value in values) send(value)
}

/**
 * Returns a [ReceiveChannel] which emits all elements of this iterable (in the same order)
 */
@ExperimentalCoroutinesApi
public fun <T> Iterable<T>.openSubscription(context: CoroutineContext = Dispatchers.Unconfined, capacity: Int = 0): ReceiveChannel<T> =
    asSequence().openSubscription(context, capacity)

/**
 * Returns a [ReceiveChannel] which emits all elements of this sequence (in the same order)
 */
@ExperimentalCoroutinesApi
public fun <T> Sequence<T>.openSubscription(context: CoroutineContext = Dispatchers.Unconfined, capacity: Int = 0): ReceiveChannel<T> =
    GlobalScope.produce(context, capacity = capacity) { forEach { send(it) } }

/**
 * Equivalent of [produce] but starting atomically. (It is guaranteed that [block] is invoked, even if the job is cancelled)
 */

@ExperimentalCoroutinesApi
internal fun <E> CoroutineScope.produceAtomic(context: CoroutineContext = EmptyCoroutineContext, capacity: Int = 0, block: suspend ProducerScope<E>.() -> Unit): ReceiveChannel<E> {
    val result = Channel<E>(capacity)

    val job = launch(context, CoroutineStart.ATOMIC) {
        try {
            coroutineScope {
                SimpleProducerScope(result, coroutineContext).block()
            }
            result.close()
        } catch (error: Throwable) {
            result.close(error)
        }
    }

    return object : ReceiveChannel<E> by result {
        override fun cancel() {
            job.cancel()
        }

        @Suppress("EXPERIMENTAL_OVERRIDE", "EXPERIMENTAL_API_USAGE")
        override fun cancel(cause: Throwable?): Boolean {
            return job.cancel(cause)
        }

        @Suppress("EXPERIMENTAL_OVERRIDE", "EXPERIMENTAL_API_USAGE")
        override fun cancel0(): Boolean {
            return job.cancel(null)
        }
    }
}

@ExperimentalCoroutinesApi
private class SimpleProducerScope<E>(override val channel: SendChannel<E>, override val coroutineContext: CoroutineContext) : ProducerScope<E>, CoroutineScope, SendChannel<E> by channel
