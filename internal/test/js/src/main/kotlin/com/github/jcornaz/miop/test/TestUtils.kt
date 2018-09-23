package com.github.jcornaz.miop.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun runTest(dispatcher: CoroutineDispatcher, block: suspend CoroutineScope.() -> Unit): dynamic = GlobalScope.promise(dispatcher, block = block)
