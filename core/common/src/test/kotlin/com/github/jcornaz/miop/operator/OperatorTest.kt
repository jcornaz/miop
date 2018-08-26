package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class OperatorTest {

    abstract fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T>

    @Test
    fun testCancel() = runTest {
        val source = Channel<Int>()
        source.operator().cancel(DummyException("something went wrong"))

        assertThrows<Exception> { source.send(0) }
    }

    @Test
    fun shouldEmitTheUpstreamErrorIfAny() = runTest {
        val exception = assertThrows<Exception> { produce<Int> { throw Exception("my exception") }.operator().first() }
        assertEquals("my exception", exception.message)
    }

    @Test
    fun shouldReturnEmptyChannelForAnEmptySource() = runTest {
        val result = emptyReceiveChannel<String>().operator()

        withTimeout(1000) {
            assertTrue(result.toList().isEmpty())
        }
    }
}
