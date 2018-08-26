package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Suspend until the cancellation of the current coroutines.
 *
 * Throw a [kotlinx.coroutines.CancellationException] as soon as the coroutines is cancelled
 */
public suspend fun awaitCancel(): Nothing = suspendCancellableCoroutine<Nothing> { }
