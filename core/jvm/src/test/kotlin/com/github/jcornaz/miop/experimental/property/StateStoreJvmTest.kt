package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import kotlinx.coroutines.experimental.timeunit.TimeUnit
import kotlinx.coroutines.experimental.withTimeout
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StateStoreJvmTest {

    @Test
    fun errorShouldBeSentToDefaultUncaughtThreadHandler() = runTest {
        var exception: Throwable? = null

        Thread.setDefaultUncaughtExceptionHandler { _, e -> exception = e }

        val barrier = Mutex(true)

        StateStore("test").apply {
            dispatch { throw Exception("my exception") }
            dispatch { barrier.unlock() ; it }
        }


        withTimeout(1, TimeUnit.SECONDS) { barrier.withLock {  } }

        assertNotNull(exception)
        assertEquals("my exception", exception?.message)
    }
}
