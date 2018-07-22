package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.launchConsumeEach
import kotlinx.coroutines.experimental.Job

/**
 * Start a coroutine which keep the variable up-to-date
 *
 * @return job of the coroutine. Shall be used to cancel the binding.
 */
fun <T> SubscribableVariable<in T>.bind(source: SubscribableValue<T>, parent: Job? = null): Job =
    source.openSubscription().launchConsumeEach(parent = parent) { set(it) }

fun <T, S, A> StateStore<S, A>.bind(source: SubscribableValue<T>, parent: Job? = null, createAction: (T) -> A): Job =
    source.openSubscription().launchConsumeEach(parent = parent) { dispatch(createAction(it)) }
