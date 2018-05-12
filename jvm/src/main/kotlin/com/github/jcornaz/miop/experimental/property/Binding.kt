package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.launchConsumeEach
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined

/**
 * Start a coroutine which keep the variable up-to-date
 *
 * @return job of the coroutine. Shall be used to cancel the binding.
 */
fun <T> SubscribableVariable<in T>.bind(source: SubscribableValue<T>, parent: Job? = null): Job =
        source.openSubscription().launchConsumeEach(Unconfined, parent = parent) { value = it }
