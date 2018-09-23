package com.github.jcornaz.miop.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual fun runTest(dispatcher: CoroutineDispatcher, block: suspend CoroutineScope.() -> Unit) = runBlocking(dispatcher, block = block)
