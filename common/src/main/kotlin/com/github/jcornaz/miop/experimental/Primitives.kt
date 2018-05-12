package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Suspend until the cancellation of the current coroutines.
 *
 * Throw a [kotlinx.coroutines.experimental.CancellationException] as soon as the coroutines is cancelled
 */
public suspend fun awaitCancel(): Nothing = suspendCancellableCoroutine<Nothing> { }
