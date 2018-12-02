package com.github.jcornaz.miop

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Consume this [ReceiveChannel] and send all elements to [target].
 *
 * **DO NOT** close [target]
 */
@ObsoleteCoroutinesApi
public suspend inline fun <T> ReceiveChannel<T>.sendTo(target: SendChannel<T>): Unit =
    consumeEach { target.send(it) }
