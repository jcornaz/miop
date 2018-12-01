package com.github.jcornaz.miop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map
import kotlin.coroutines.CoroutineContext

internal expect val defaultConcurrency: Int

/**
 * Start a parallel pipeline.
 *
 * This method will concurrently call [pipeline] (given the [concurrency]) and return a channel to which all results are merged.
 *
 * Usage example:
 * ```kotlin
 * channel
 *   .filter { it % 2 } // <-- sequential
 *   .parallel(3) { // <-- Start 3 parallel pipeline
 *
 *     // build a pipeline (this lambda will be invoked 3 times concurrently)
 *     filter { it > 0 }
 *       .map { it.toString() }
 *
 *   } // <-- Join the parallel channels
 *   .map { it.length } // <-- sequential again
 *   .consumeEach {
 *     println(it)
 *   }
 * ```
 *
 * Order of elements in the result is not guaranteed.
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> ReceiveChannel<T>.parallel(concurrency: Int = defaultConcurrency, pipeline: ReceiveChannel<T>.() -> ReceiveChannel<R>): ReceiveChannel<R> =
    transform { input, output ->
        repeat(concurrency) { _ ->
            launch(Dispatchers.Default, start = CoroutineStart.ATOMIC) {
                input.map(Dispatchers.Default) { it }.pipeline().sendTo(output)
            }
        }
    }

/**
 * Parallel version of [map].
 *
 * [transform] is executed concurrently accordingly to [concurrency].
 *
 * Order of elements is not guaranteed.
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> ReceiveChannel<T>.parallelMap(context: CoroutineContext = Dispatchers.Default, concurrency: Int = defaultConcurrency, transform: suspend (T) -> R): ReceiveChannel<R> =
    transform { input, output ->
        repeat(concurrency) {
            launch(Dispatchers.Default, start = CoroutineStart.ATOMIC) {
                input.map(context, transform).sendTo(output)
            }
        }
    }

/**
 * Parallel version of [map].
 *
 * [predicate] is executed concurrently accordingly to [concurrency].
 *
 * Order of elements is not guaranteed.
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T> ReceiveChannel<T>.parallelFilter(context: CoroutineContext = Dispatchers.Default, concurrency: Int = defaultConcurrency, predicate: suspend (T) -> Boolean): ReceiveChannel<T> =
    transform { input, output ->
        repeat(concurrency) {
            launch(Dispatchers.Default, start = CoroutineStart.ATOMIC) {
                input.filter(context, predicate).sendTo(output)
            }
        }
    }
