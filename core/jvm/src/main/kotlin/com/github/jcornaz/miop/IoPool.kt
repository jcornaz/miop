package com.github.jcornaz.miop

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.IO

@Deprecated("Use kotlinx.coroutines built-in IO dispatcher", ReplaceWith("IO", "kotlinx.coroutines.IO"))
actual val IoPool: CoroutineDispatcher get() = IO
