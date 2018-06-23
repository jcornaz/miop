package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.DefaultDispatcher

actual val IoPool: CoroutineDispatcher get() = DefaultDispatcher