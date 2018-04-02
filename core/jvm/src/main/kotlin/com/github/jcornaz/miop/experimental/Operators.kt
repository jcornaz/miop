package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

public object Channels {
    public fun <T1, T2, R> combineLatest(
            source1: ReceiveChannel<T1>,
            source2: ReceiveChannel<T2>,
            context: CoroutineContext = Unconfined,
            capacity: Int = Channel.CONFLATED,
            combine: suspend (T1, T2) -> R
    ): ReceiveChannel<R> = produce(context, capacity) {
        try {
            var v1 = source1.receive()
            var v2 = source2.receive()

            send(combine(v1, v2))

            launch(coroutineContext) { source1.consumeEach { v1 = it; send(combine(it, v2)) } }
            launch(coroutineContext) { source2.consumeEach { v2 = it; send(combine(v1, it)) } }
        } catch (t: Throwable) {
            source1.cancel()
            source2.cancel()
        }
    }
}

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
