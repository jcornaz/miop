package com.github.jcornaz.miop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

internal expect val defaultConcurrency: Int

/**
 * Start a parallel pipeline.
 *
 * This method will concurrently call [pipe] (given the [concurrency]) and return a channel to which all results are merged.
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
public fun <T, R> ReceiveChannel<T>.parallel(concurrency: Int = defaultConcurrency, pipe: ReceiveChannel<T>.() -> ReceiveChannel<R>): ReceiveChannel<R> =
    GlobalScope.produce(Dispatchers.Unconfined) {
        repeat(concurrency) {
            launch { pipe().sendTo(channel) }
        }
    }
