package com.github.jcornaz.miop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ObsoleteCoroutinesApi
internal suspend inline fun <T> ReceiveChannel<T>.sendTo(target: SendChannel<T>): Unit =
    consumeEach { target.send(it) }

@ObsoleteCoroutinesApi
@UseExperimental(ExperimentalCoroutinesApi::class)
internal fun <T> CoroutineScope.pipe(source: ReceiveChannel<T>, destination: SendChannel<T>, context: CoroutineContext = EmptyCoroutineContext): Job =
    launch(context) { source.sendTo(destination) }
