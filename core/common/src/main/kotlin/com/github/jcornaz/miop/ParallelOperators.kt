package com.github.jcornaz.miop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
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
 */
@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> ReceiveChannel<T>.parallel(concurrency: Int = defaultConcurrency, pipeline: ReceiveChannel<T>.() -> ReceiveChannel<R>): ReceiveChannel<R> =
    GlobalScope.produceAtomic(Dispatchers.Default) {
        repeat(concurrency) { _ ->
            pipe(map(Dispatchers.Default) { it }.pipeline(), channel)
        }
    }

@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> ReceiveChannel<T>.parallelMap(context: CoroutineContext = Dispatchers.Default, concurrency: Int = defaultConcurrency, transform: suspend (T) -> R): ReceiveChannel<R> =
    GlobalScope.produceAtomic(Dispatchers.Default) {
        repeat(concurrency) { _ ->
            pipe(map(context, transform), channel)
        }
    }

@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T> ReceiveChannel<T>.parallelFilter(context: CoroutineContext = Dispatchers.Default, concurrency: Int = defaultConcurrency, predicate: suspend (T) -> Boolean): ReceiveChannel<T> =
    GlobalScope.produceAtomic(Dispatchers.Default) {
        repeat(concurrency) { _ ->
            pipe(filter(context, predicate), channel)
        }
    }
