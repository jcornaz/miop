package com.github.jcornaz.miop.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.fail

inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T =
        try {
            block()
            null
        } catch (t: Throwable) {
            t as? T ?: fail("${T::class} was expected but ${t::class} was thrown")
        } ?: fail("${T::class} was expected but no exception was thrown")

expect fun runTest(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit)

suspend fun delayTest() = delay(200)
