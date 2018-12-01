package com.github.jcornaz.miop

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach

@ObsoleteCoroutinesApi
internal suspend inline fun <T> ReceiveChannel<T>.sendTo(target: SendChannel<T>): Unit =
    consumeEach { target.send(it) }
