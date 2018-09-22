package com.github.jcornaz.miop.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual fun runTest(block: suspend CoroutineScope.() -> Unit) = runBlocking(block = block)
