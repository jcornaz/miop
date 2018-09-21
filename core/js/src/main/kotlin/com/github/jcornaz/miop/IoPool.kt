package com.github.jcornaz.miop

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val IoPool: CoroutineDispatcher get() = Dispatchers.Default
