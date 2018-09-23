package com.github.jcornaz.miop.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.coroutines.CoroutineContext

actual fun runTest(context: CoroutineContext, block: suspend CoroutineScope.() -> Unit): dynamic = GlobalScope.promise(context, block = block)
