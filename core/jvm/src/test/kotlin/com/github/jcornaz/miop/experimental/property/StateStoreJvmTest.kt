package com.github.jcornaz.miop.experimental.property

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StateStoreJvmTest {

    @Test
    fun errorShouldBeSentToDefaultUncaughtThreadHandler() {
        var exception: Throwable? = null

        Thread.setDefaultUncaughtExceptionHandler { _, e -> exception = e }

        StateStore("test").dispatch { throw Exception("my exception") }

        assertNotNull(exception)
        assertEquals("my exception", exception?.message)
    }
}
