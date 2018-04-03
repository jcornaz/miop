package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce

private val EmptyReceiveChannel = produce<Nothing>(Unconfined) { }

public fun <E> emptyReceiveChannel(): ReceiveChannel<E> = EmptyReceiveChannel

public fun <E> receiveChannelOf(vararg values: E): ReceiveChannel<E> = produce(Unconfined, values.size) {
    for (value in values) send(value)
}