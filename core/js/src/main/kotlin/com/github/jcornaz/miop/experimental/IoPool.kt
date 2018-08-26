package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DefaultDispatcher

actual val IoPool: CoroutineDispatcher get() = DefaultDispatcher