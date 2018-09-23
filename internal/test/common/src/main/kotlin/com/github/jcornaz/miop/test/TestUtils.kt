package com.github.jcornaz.miop.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.fail

inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T =
        try {
            block()
            null
        } catch (t: Throwable) {
            t as? T ?: fail("${T::class} was expected but ${t::class} was thrown")
        } ?: fail("${T::class} was expected but no exception was thrown")

expect fun runTest(dispatcher: CoroutineDispatcher = Dispatchers.Unconfined, block: suspend CoroutineScope.() -> Unit)
