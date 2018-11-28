package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.failedReceiveChannel
import com.github.jcornaz.miop.test.AsyncTest
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class OperatorTest : AsyncTest() {

    abstract fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T>

    @Test
    fun testCancel() = runTest {
        val source = Channel<Int>()
        source.operator().cancel()

        assertThrows<Exception> { source.send(0) }
    }

    @Test
    fun shouldEmitTheUpstreamErrorIfAny() = runTest {
        val exception = assertThrows<DummyException> { failedReceiveChannel<Int>(DummyException("my exception")).operator().first() }
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
