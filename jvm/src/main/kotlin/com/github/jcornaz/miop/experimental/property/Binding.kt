package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.launchConsumeEach
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Start a coroutine which keep the variable up-to-date
 *
 * @return job of the coroutine. Shall be used to cancel the binding.
 */
fun <T> SubscribableVariable<in T>.bind(source: SubscribableValue<T>, parent: Job? = null): Job =
        bind(source.openSubscription(), parent)

/**
 * Start a coroutine which keep the variable up-to-date
 *
 * @return job of the coroutine. Shall be used to cancel the binding.
 */
fun <T> SubscribableVariable<in T>.bind(source: ReceiveChannel<T>, parent: Job? = null): Job =
        source.launchConsumeEach(Unconfined, parent = parent) { value = it }

/**
 * Start a coroutine which keep both variables up-to-date
 *
 * @return job of the coroutine. Shall be used to cancel the binding.
 */
fun <T> SubscribableVariable<T>.bindBidirectional(other: SubscribableVariable<T>, parent: Job? = null): Job {
    val result = Job(parent)

    bind(other, result)
    other.bind(this, result)

    return result
}
