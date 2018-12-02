package com.github.jcornaz.miop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

internal expect val defaultParallelism: Int

/**
 * Start a parallel pipeline.
 *
 * This method will concurrently call [pipeline] (given the [parallelism]) and return a channel to which all results are merged.
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
public fun <T, R> ReceiveChannel<T>.parallel(parallelism: Int = defaultParallelism, pipeline: ReceiveChannel<T>.() -> ReceiveChannel<R>): ReceiveChannel<R> =
    transform { input, output ->
        val exceptions = HashSet<Throwable>()
        val mutex = Mutex()

        repeat(parallelism) { index ->
            println("start job $index/$parallelism")
            launch(Dispatchers.Default, start = CoroutineStart.ATOMIC) {
                try {
                    input.map(Dispatchers.Default) { it }.pipeline().sendTo(output)
                } catch (error: Throwable) {
                    val actualError = if (error is CancellationException) error.cause ?: return@launch else error
                    mutex.withLock {
                        if (exceptions.add(actualError)) throw error
                    }
                }
            }
        }
    }

/**
 * Parallel version of [map].
 *
 * [transform] is executed concurrently accordingly to [parallelism].
 *
 * Order of elements is not guaranteed.
 *
 * @see parallel
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> ReceiveChannel<T>.parallelMap(context: CoroutineContext = Dispatchers.Default, parallelism: Int = defaultParallelism, transform: suspend (T) -> R): ReceiveChannel<R> =
    parallel(parallelism) { map(context, transform) }

/**
 * Parallel version of [map].
 *
 * [predicate] is executed concurrently accordingly to [parallelism].
 *
 * Order of elements is not guaranteed.
 *
 * @see parallel
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T> ReceiveChannel<T>.parallelFilter(context: CoroutineContext = Dispatchers.Default, parallelism: Int = defaultParallelism, predicate: suspend (T) -> Boolean): ReceiveChannel<T> =
    parallel(parallelism) { filter(context, predicate) }
