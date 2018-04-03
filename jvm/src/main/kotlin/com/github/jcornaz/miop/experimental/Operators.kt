package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.*
import kotlin.coroutines.experimental.CoroutineContext

public object Channels {

    public fun <T> merge(vararg sources: ReceiveChannel<T>): ReceiveChannel<T> = produce(Unconfined) {
        sources.forEach { source ->
            launch(coroutineContext) {
                source.consumeEach { send(it) }
            }
        }
    }


    public fun <T1, T2, R> combineLatest(
            source1: ReceiveChannel<T1>,
            source2: ReceiveChannel<T2>,
            context: CoroutineContext = Unconfined,
            capacity: Int = Channel.CONFLATED,
            combine: suspend (T1, T2) -> R
    ): ReceiveChannel<R> = produce(context, capacity) {
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

public fun <T> ReceiveChannel<T>.mergeWith(vararg others: ReceiveChannel<T>): ReceiveChannel<T> =
        Channels.merge(this, *others)

public fun <T1, T2, R> ReceiveChannel<T1>.combineLatestWith(
        other: ReceiveChannel<T2>,
        context: CoroutineContext = Unconfined,
        capacity: Int = Channel.CONFLATED,
        combine: suspend (T1, T2) -> R
): ReceiveChannel<R> = Channels.combineLatest(this, other, context, capacity, combine)

public fun <T, R> ReceiveChannel<T>.switchMap(transform: (T) -> ReceiveChannel<R>): ReceiveChannel<R> = produce(Unconfined) {
    var job: Job? = null
    consumeEach { element ->
        job?.cancelAndJoin()
        job = launch(coroutineContext) {
            transform(element).consumeEach { send(it) }
        }
    }
}