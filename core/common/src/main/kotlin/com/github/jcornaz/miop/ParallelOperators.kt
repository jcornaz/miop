package com.github.jcornaz.miop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

internal expect val defaultConcurrency: Int

@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> ReceiveChannel<T>.parallel(concurrency: Int = defaultConcurrency, pipe: ReceiveChannel<T>.() -> ReceiveChannel<R>): ReceiveChannel<R> =
    GlobalScope.produce(Dispatchers.Unconfined) {
        repeat(concurrency) {
            launch { pipe().sendTo(channel) }
        }
    }
