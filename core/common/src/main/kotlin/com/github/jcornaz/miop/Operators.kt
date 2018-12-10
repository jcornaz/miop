package com.github.jcornaz.miop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

/**
 * Operators for [ReceiveChannel]
 */
public object Channels {

    /**
     * Returns a [ReceiveChannel] through which elements of all given sources are sent as soon as received.
     *
     * Cancelling the result channel will cancel all the sources.
     *
     * If one source is closed with an exception, the result channel will be closed with the same exception and all other sources will be cancelled.
     */
    @ObsoleteCoroutinesApi
    @UseExperimental(ExperimentalCoroutinesApi::class)
    public fun <T> merge(vararg sources: ReceiveChannel<T>): ReceiveChannel<T> =
        GlobalScope.produceAtomic(Dispatchers.Unconfined) {
            try {
                coroutineScope {
                    sources.forEach { source ->
                        launch(Dispatchers.Unconfined) {
                            source.consumeEach { send(it) }
                        }
                    }
                }
            } finally {
                sources.forEach(ReceiveChannel<T>::cancel)
            }
        }

    /**
     * Returns a [ReceiveChannel] which combine the most recently emitted items from each sources.
     *
     * Cancelling the result channel will cancel all the sources.
     *
     * If one source is closed with an exception, the result channel will be closed with the same exception and all other sources will be cancelled.
     *
     * @param context Context on which execute [combine]
     * @param combine Function to combine elements from the sources
     */
    @Suppress("UNCHECKED_CAST")
    @ObsoleteCoroutinesApi
    @UseExperimental(ExperimentalCoroutinesApi::class)
    public fun <T1, T2, R> combineLatest(
        source1: ReceiveChannel<T1>,
        source2: ReceiveChannel<T2>,
        context: CoroutineContext = Dispatchers.Unconfined,
        combine: suspend (T1, T2) -> R
    ): ReceiveChannel<R> = GlobalScope.produceAtomic(context) {
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
 * Pipeline version of [produce].
 *
 * The [block] receive and input [ReceiveChannel] which can be used to receive upstream elements and an output [SendChannel] which can be used to send elements downstream.
 *
 * The upstream channel is consumed. When [block] is finished, the upstream channel is cancelled (if not already completed).
 * If the [block] fails the upstream channel is cancelled with the cause exception.
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <I, O> ReceiveChannel<I>.transform(
    context: CoroutineContext = Dispatchers.Unconfined,
    capacity: Int = 0,
    block: suspend CoroutineScope.(input: ReceiveChannel<I>, output: SendChannel<O>) -> Unit
): ReceiveChannel<O> = GlobalScope.produceAtomic(context, capacity) {
    try {
        coroutineScope { block(this@transform, channel) }
    } finally {
        this@transform.cancel()
    }
}

/**
 * Return a [ReceiveChannel] through which elements of all given sources (including this channel) are sent as soon as received.
 *
 * Cancelling the result channel will cancel all the sources.
 *
 * If one source is closed with an exception, the result channel will be closed with the same exception and all other sources will be cancelled.
 */
@ObsoleteCoroutinesApi
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
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T1, T2, R> ReceiveChannel<T1>.combineLatestWith(
    other: ReceiveChannel<T2>,
    context: CoroutineContext = Dispatchers.Unconfined,
    combine: suspend (T1, T2) -> R
): ReceiveChannel<R> = Channels.combineLatest(this, other, context, combine)

/**
 * Return a [ReceiveChannel] which emits the items of the latest result of [transform] which is called for each elements of this channel.
 *
 * Each time a new element is received from this channel, the previous result of [transform] is cancelled, and [transform] is called to get the new source to consume and to send elements
 * the result of [switchMap]
 *
 * Cancelling the result channel will cancel the current source (latest result of [transform]).
 *
 * If the current source source is closed with an exception, the result channel will be closed with the same exception.
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> ReceiveChannel<T>.switchMap(context: CoroutineContext = Dispatchers.Unconfined, transform: suspend (T) -> ReceiveChannel<R>): ReceiveChannel<R> = transform { input, output ->
    var job: Job? = null
    input.consumeEach { element ->
        job?.cancelAndJoin()
        job = launch(context) {
            transform(element).consumeEach { output.send(it) }
        }
    }
}

/**
 * Launches new coroutine which consume the channel and execute [action] for each element.
 *
 * It allows to write `channel.launchConsumeEach(context) { ... }` instead of `launch(context) { channel.consumeEach { ... } }`
 */
@Deprecated("Standalone coroutine builders are deprecated, use CoroutineScope.launch { source.consumeEach {} } instead", level = DeprecationLevel.ERROR)
@UseExperimental(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
public fun <E> ReceiveChannel<E>.launchConsumeEach(
    context: CoroutineContext = Dispatchers.Unconfined,
    start: CoroutineStart = CoroutineStart.ATOMIC,
    parent: Job?,
    action: suspend (E) -> Unit
): Job {
    val actualContext = if (parent == null) context else context + parent

    return GlobalScope.launch(actualContext, start) {
        consumeEach { action(it) }
    }
}

/**
 * Launches new coroutine which consume the channel and execute [action] for each element.
 *
 * It allows to write `channel.launchConsumeEach(context) { ... }` instead of `launch(context) { channel.consumeEach { ... } }`
 */
@Deprecated(
    message = "Standalone coroutine builders are deprecated, use CoroutineScope.launch { source.consumeEach {} } instead",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        expression = "GlobalScope.launch(context, start, onCompletion = consumes()) { consumeEach { action(it) } }",
        imports = ["kotlinx.coroutines.GlobalScope", "kotlinx.coroutines.launch", "kotlinx.coroutines.channels.consumes", "kotlinx.coroutines.channels.consumeEach"]
    )
)
@UseExperimental(ObsoleteCoroutinesApi::class, ExperimentalCoroutinesApi::class)
public fun <E> ReceiveChannel<E>.launchConsumeEach(
    context: CoroutineContext = Dispatchers.Unconfined,
    start: CoroutineStart = CoroutineStart.ATOMIC,
    action: suspend (E) -> Unit
): Job = GlobalScope.launch(context, start) {
    consumeEach { action(it) }
}

/**
 * Returns a [ReceiveChannel] which emits the element of this channel, unless the element is equal to the last emitted element.
 *
 * Example: for the source: [1, 2, 2, 1, 2] [distinctUntilChanged] will emit: [1, 2, 1, 2]
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <E> ReceiveChannel<E>.distinctUntilChanged(): ReceiveChannel<E> = transform { input, output ->
    var latest = try {
        input.receive()
    } catch (error: ClosedReceiveChannelException) {
        return@transform
    }

    output.send(latest)

    input.consumeEach { elt ->
        if (elt != latest) {
            output.send(elt)
            latest = elt
        }
    }
}

/**
 * Returns a [ReceiveChannel] which emits the element of this channel, unless the element has the same reference as the last emitted element.
 */
@ObsoleteCoroutinesApi
public fun <E> ReceiveChannel<E>.distinctReferenceUntilChanged(): ReceiveChannel<E> = transform { input, output ->
    var latest = try {
        input.receive()
    } catch (error: ClosedReceiveChannelException) {
        return@transform
    }

    output.send(latest)

    input.consumeEach { elt ->
        if (elt !== latest) {
            output.send(elt)
            latest = elt
        }
    }
}

/**
 * Returns a [ReceiveChannel] emitting the elements of this channel which are instance of [E]
 */
@ObsoleteCoroutinesApi
public inline fun <reified E> ReceiveChannel<*>.filterIsInstance(): ReceiveChannel<E> = transform { input, output ->
    input.consumeEach {
        if (it is E) output.send(it)
    }
}

/**
 * Filters out elements emitted by the source that are rapidly followed by another emitted element.
 *
 * Only emit an element received if the given [timeSpan] (in millisecond) has passed without it emitting another item.
 */
@ObsoleteCoroutinesApi
public fun <E> ReceiveChannel<E>.debounce(timeSpan: Long): ReceiveChannel<E> = transform { input, output ->
    var job: Job? = null
    input.consumeEach { element ->
        job?.cancel()
        job = launch(coroutineContext) {
            delay(timeSpan)
            output.send(element)
        }
    }
}

/**
 * Buffer emissions, allowing the producer to not wait on the consumer.
 *
 * @param capacity Max number of elements to buffer. Once the limit is reached, the producer will suspend. [Channel.UNLIMITED] and [Channel.CONFLATED] can be used.
 */
@ObsoleteCoroutinesApi
public fun <E> ReceiveChannel<E>.buffer(capacity: Int = Channel.UNLIMITED): ReceiveChannel<E> =
    transform(capacity = capacity) { input, output -> input.consumeEach { output.send(it) } }

/**
 * Buffers at most one element and conflates all subsequent emissions,
 * so that the receiver always gets the most recently sent element.
 * Back-to-send sent elements are _conflated_ -- only the the most recently sent element is received,
 * while previously sent elements **are lost**.
 *
 * The source never have to suspend to wait on the consumer.
 *
 * @see ConflatedChannel
 */
@ObsoleteCoroutinesApi
@Suppress("NOTHING_TO_INLINE")
public inline fun <E> ReceiveChannel<E>.conflate(): ReceiveChannel<E> = buffer(Channel.CONFLATED)

/**
 * Returns a channel emitting snapshots of the window of the given [size]
 * sliding along the source with the given [step], where each
 * snapshot is a list.
 *
 * Several last lists may have less elements than the given [size].
 *
 * Both [size] and [step] must be positive and can be greater than the number of elements emitted by this source.
 * @param size the number of elements to take in each window
 * @param step the number of elements to move the window forward by on an each step, by default 1
 * @param partialWindows controls whether or not to keep partial windows in the end if any,
 * by default `false` which means partial windows won't be preserved
 */
@ObsoleteCoroutinesApi
public fun <E> ReceiveChannel<E>.windowed(size: Int, step: Int = 1, partialWindows: Boolean = false): ReceiveChannel<List<E>> = transform { input, output ->
    require(size > 0 && step > 0) { "size and step have to be greater than 0 but size was $size and step was $step" }

    var window = ArrayList<E>(size)
    var countDown = 0

    input.consumeEach { element ->
        if (countDown > 0) {
            --countDown
            return@consumeEach
        }

        window.add(element)

        if (window.size == size) {
            output.send(window)

            val newWindow = ArrayList<E>(size)

            if (step < window.size) {
                newWindow.addAll(window.subList(step, window.size))
            } else {
                countDown = step - window.size
            }

            window = newWindow
        }
    }

    if (window.isNotEmpty() && partialWindows) {
        window.asSequence().windowed(size, step, true).forEach { output.send(it) }
    }
}

/**
 * Splits aggregate received elements into lists not exceeding the given [size].
 *
 * The last list in the resulting channel may have less elements than the given [size].
 *
 * @param size the number of elements to take in each list, must be positive and can be greater than the number of elements emitted by this source.
 */
@ObsoleteCoroutinesApi
@Suppress("NOTHING_TO_INLINE")
public inline fun <E> ReceiveChannel<E>.chunked(size: Int): ReceiveChannel<List<E>> = windowed(size, size, true)

/**
 * Send downstream each intermediate result of accumulating the emitted elements using the given [operation] function, and starting with a [initial].
 *
 * Example: `receiveChannelOf(1, 2, 3).san(0) { acc, element -> acc + element }` will emit: `[0, 1, 3, 6]`
 */
@ObsoleteCoroutinesApi
public fun <T, R> ReceiveChannel<T>.scan(initial: R, operation: (acc: R, T) -> R): ReceiveChannel<R> = transform { input, output ->
    output.send(initial)
    var result = initial
    input.consumeEach {
        result = operation(result, it)
        output.send(result)
    }
}

/**
 * Send downstream each intermediate result of accumulating the emitted elements using the given [operation] function.
 *
 * Example: `receiveChannelOf(1, 2, 3).san { acc, element -> acc + element }` will emit: `[1, 3, 6]`
 */
@ObsoleteCoroutinesApi
public fun <T> ReceiveChannel<T>.scan(operation: (acc: T, T) -> T): ReceiveChannel<T> = transform { input, output ->
    var result = try {
        receive()
    } catch (closedChannel: ClosedReceiveChannelException) {
        return@transform
    }

    output.send(result)

    input.consumeEach {
        result = operation(result, it)
        output.send(result)
    }
}
