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
@Deprecated("Use produce(Iterable) instead", ReplaceWith("GlobalScope.produce(this, context)", "kotlinx.coroutines.GlobalScope", "kotlinx.coroutines.Dispatchers"))
@Suppress("UNUSED_PARAMETER")
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T> Iterable<T>.openSubscription(context: CoroutineContext = Dispatchers.Unconfined, capacity: Int = 0): ReceiveChannel<T> =
    GlobalScope.produce(this, context)

/**
 * Returns a [ReceiveChannel] which emits all elements of this sequence (in the same order)
 */
@Deprecated("Use produce(Sequence) instead", ReplaceWith("GlobalScope.produce(this, context)", "kotlinx.coroutines.GlobalScope", "kotlinx.coroutines.Dispatchers"))
@Suppress("UNUSED_PARAMETER")
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T> Sequence<T>.openSubscription(context: CoroutineContext = Dispatchers.Unconfined, capacity: Int = 0): ReceiveChannel<T> =
    GlobalScope.produce(this, context)

/**
 * Returns a [ReceiveChannel] which emits all elements of this iterable (in the same order)
 */
@ExperimentalCoroutinesApi
public fun <T> CoroutineScope.produce(elements: Iterable<T>, context: CoroutineContext = Dispatchers.Default): ReceiveChannel<T> =
    produce(context, capacity = if (elements is Collection) elements.size else 0) { elements.forEach { send(it) } }

/**
 * Returns a [ReceiveChannel] which emits all elements of this sequence (in the same order)
 */
@ExperimentalCoroutinesApi
public fun <T> CoroutineScope.produce(elements: Sequence<T>, context: CoroutineContext = Dispatchers.Default): ReceiveChannel<T> =
    produce(elements.asIterable(), context)

/**
 * Equivalent of [produce] but starting atomically. (It is guaranteed that [block] is invoked, even if the job is cancelled)
 */
@ExperimentalCoroutinesApi
public fun <E> CoroutineScope.produceAtomic(context: CoroutineContext = EmptyCoroutineContext, capacity: Int = 0, block: suspend ProducerScope<E>.() -> Unit): ReceiveChannel<E> {
    val result = Channel<E>(capacity)

    val exceptionHandler = context[CoroutineExceptionHandler]
        ?: coroutineContext[CoroutineExceptionHandler]
        ?: CoroutineExceptionHandler { _, _ -> /* ignore */ }

    val job = launch(context + exceptionHandler, CoroutineStart.ATOMIC) {
        try {
            coroutineScope {
                SimpleProducerScope(result, coroutineContext).block()
            }
            result.close()
        } catch (error: Throwable) {
            result.close(error)
            throw error
        }
    }

    return object : ReceiveChannel<E> by result {
        override fun cancel() {
            job.cancel()
        }

        @Suppress("EXPERIMENTAL_OVERRIDE", "EXPERIMENTAL_API_USAGE", "OverridingDeprecatedMember", "DEPRECATION")
        override fun cancel(cause: Throwable?): Boolean {
            return job.cancel(cause)
        }

        @Suppress("EXPERIMENTAL_OVERRIDE", "EXPERIMENTAL_API_USAGE", "OverridingDeprecatedMember", "DEPRECATION")
        override fun cancel0(): Boolean {
            return job.cancel(null)
        }
    }
}

@ExperimentalCoroutinesApi
private class SimpleProducerScope<E>(override val channel: SendChannel<E>, override val coroutineContext: CoroutineContext) : ProducerScope<E>, CoroutineScope, SendChannel<E> by channel
