package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.suspendCancellableCoroutine

public suspend fun awaitCancel() = suspendCancellableCoroutine<Unit> { }
