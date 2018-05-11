package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Suspend until th
 */
public suspend fun awaitCancel(): Nothing = suspendCancellableCoroutine<Nothing> { }
