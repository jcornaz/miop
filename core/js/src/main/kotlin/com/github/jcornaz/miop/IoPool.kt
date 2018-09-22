package com.github.jcornaz.miop

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Deprecated("Use Dispatechers.IO from JVM", ReplaceWith("Dispatchers.Default", "kotlinx.coroutines.Dispatchers"))
actual val IoPool: CoroutineDispatcher get() = Dispatchers.Default
