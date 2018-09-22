package com.github.jcornaz.miop

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Deprecated("Use kotlinx.coroutines built-in IO dispatcher", ReplaceWith("Dispatchers.IO", "kotlinx.coroutines.Dispatchers", "kotlinx.coroutines.IO"))
public actual val IoPool: CoroutineDispatcher get() = Dispatchers.IO
