package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.*
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Operators for [ReceiveChannel]
 */
public object Channels {

    /**
     * Return a [ReceiveChannel] through which elements of all given sources are sent as soon as received.
     *
     * Cancelling the result channel will cancel all the sources.
     *
     * If one source is closed with an exception, the result channel will be closed with the same exception and all other sources will be cancelled.
     */
    public fun <T> merge(vararg sources: ReceiveChannel<T>): ReceiveChannel<T> = produce(Unconfined) {

        // Necessary to not deliver the exception to the uncaught exception handler
        val context = coroutineContext + CoroutineExceptionHandler { _, throwable ->
            coroutineContext[Job]!!.cancel(throwable)
        }

        sources.forEach { source ->
            launch(context) {
                source.consumeEach { send(it) }
            }
        }
    }

    /**
     * Return a [ReceiveChannel] which combine the most recently emitted items from each sources.
     *
     * Cancelling the result channel will cancel all the sources.
     *
     * If one source is closed with an exception, the result channel will be closed with the same exception and all other sources will be cancelled.
     *
     * @param context Context on which execute [combine]
     * @param combine Function to combine elements from the sources
     */
    public fun <T1, T2, R> combineLatest(
            source1: ReceiveChannel<T1>,
            source2: ReceiveChannel<T2>,
            context: CoroutineContext = Unconfined,
            combine: suspend (T1, T2) -> R
    ): ReceiveChannel<R> = produce(context) {
        var v1: T1? = null
        var v2: T2? = null

        var hasV1 = false
        var hasV2 = false

        merge(
                source1.map { { v1 = it; hasV1 = true; hasV2 } },
                source2.map { { v2 = it; hasV2 = true; hasV1 } }
        ).consumeEach { fct ->
            if (fct()) {

                @Suppress("UNCHECKED_CAST")
                send(combine(v1 as T1, v2 as T2))
            }
        }
    }
}

/**
 * Return a [ReceiveChannel] through which elements of all given sources (including this channel) are sent as soon as received.
 *
 * Cancelling the result channel will cancel all the sources.
 *
 * If one source is closed with an exception, the result channel will be closed with the same exception and all other sources will be cancelled.
 */
public fun <T> ReceiveChannel<T>.mergeWith(vararg others: ReceiveChannel<T>): ReceiveChannel<T> =
        Channels.merge(this, *others)

/**
 * Return a [ReceiveChannel] which combine the most recently emitted items from each sources.
 *
 * Cancelling the result channel will cancel all the sources.
 *
 * If one source is closed with an exception, the result channel will be closed with the same exception and all other sources will be cancelled.
 *
 * @param context Context on which execute [combine]
 * @param combine Function to combine elements from the sources
 */
public fun <T1, T2, R> ReceiveChannel<T1>.combineLatestWith(
        other: ReceiveChannel<T2>,
        context: CoroutineContext = Unconfined,
        combine: suspend (T1, T2) -> R
): ReceiveChannel<R> = Channels.combineLatest(this, other, context, combine)

/**
 * Return a [ReceiveChannel] which emits the items of the latest result of [transform] which is called for each elements of this channel.
 *
 * Each time a new element is received from this channel, the previous result of [transform] is cancelled, and [transform] is called to get the new source to consume and to send elements the result of [switchMap]
 *
 * Cancelling the result channel will cancel the current source (latest result of [transform]).
 *
 * If the current source source is closed with an exception, the result channel will be closed with the same exception.
 */
public fun <T, R> ReceiveChannel<T>.switchMap(transform: suspend (T) -> ReceiveChannel<R>): ReceiveChannel<R> = produce(Unconfined) {
    var job: Job? = null
    consumeEach { element ->
        job?.cancelAndJoin()
        job = launch(coroutineContext) {
            try {
                transform(element).consumeEach { send(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                close(t)
            }
        }
    }
}

/**
 * Launches new coroutine which consume the channel and execute [action] for each element.
 *
 * It allows to write `channel.launchConsumeEach { ... }` instead of `launch { channel.consumeEach { ... } }`
 */
public fun <E> ReceiveChannel<E>.launchConsumeEach(
        context: CoroutineContext = DefaultDispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        parent: Job? = null,
        action: suspend (E) -> Unit
): Job = launch(context, start, parent) {
    consumeEach { action(it) }
}

/**
 * Returns a [ReceiveChannel] which emits the element of this channel, unless the element is equal to the last emitted element.
 *
 * Example: for the source: [1, 2, 2, 1, 2] [distinctUntilChanged] will emit: [1, 2, 1, 2]
 */
public fun <E> ReceiveChannel<E>.distinctUntilChanged(): ReceiveChannel<E> = produce(Unconfined) {
    consume {
        var latest = receive()
        send(latest)

        for (elt in this) {
            if (elt != latest) {
                send(elt)
                latest = elt
            }
        }
    }
}
