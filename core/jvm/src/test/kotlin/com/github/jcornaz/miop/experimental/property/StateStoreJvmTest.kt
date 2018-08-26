package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.timeunit.TimeUnit
import kotlinx.coroutines.withTimeout
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
