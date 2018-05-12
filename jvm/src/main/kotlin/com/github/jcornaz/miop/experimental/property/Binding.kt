package com.github.jcornaz.miop.experimental.property

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

fun <T> SubscribableVariable<in T>.bind(source: SubscribableValue<T>): Job = launch(Unconfined) {
    source.openSubscription().consumeEach { value = it }
}
