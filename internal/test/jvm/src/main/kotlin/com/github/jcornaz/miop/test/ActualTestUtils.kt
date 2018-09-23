package com.github.jcornaz.miop.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

actual fun runTest(context: CoroutineContext, block: suspend CoroutineScope.() -> Unit) = runBlocking(context, block = block)
