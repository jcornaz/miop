package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

private val ioThreadCount = AtomicInteger()

actual val IoPool: CoroutineDispatcher =
        Executors.newCachedThreadPool { Thread(it, "IO thread ${ioThreadCount.incrementAndGet()}").apply { isDaemon = true } }
                .asCoroutineDispatcher()
