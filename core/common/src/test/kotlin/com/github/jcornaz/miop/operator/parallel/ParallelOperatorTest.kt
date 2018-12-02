package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.failedReceiveChannel
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.delayTest
import com.github.jcornaz.miop.test.measureTimeMillis
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class ParallelOperatorTest {

    abstract fun <T> ReceiveChannel<T>.identityDelayedOperation(parallelism: Int, delayTime: Long): ReceiveChannel<T>

    @Test
    fun testCancel() = runTest {
        val source = Channel<Int>()
        source.identityDelayedOperation(1, 0).cancel()

        delayTest()

        assertThrows<Exception> { source.send(0) }
    }

    @Test
    fun shouldEmitTheUpstreamErrorIfAny() = runTest {
        val source = failedReceiveChannel<Int>(DummyException("my exception")).identityDelayedOperation(1, 0)

        val exception = assertThrows<DummyException> { source.count() }

        assertEquals("my exception", exception.message)

        delayTest()
    }

    @Test
    fun shouldReturnEmptyChannelForAnEmptySource() = runTest {
        val result = emptyReceiveChannel<String>().identityDelayedOperation(1, 0)

        withTimeout(1000) {
            assertTrue(result.toList().isEmpty())
        }
    }

    @Test
    fun shouldParallelize() = runTest {
        val channel = receiveChannelOf(1, 2, 3, 4).identityDelayedOperation(4, 500)

        val time = measureTimeMillis { channel.count() }

        assertTrue(time < 600, "should take less than 700 ms but took $time ms")
    }

    @Test
    fun shouldNoExceedGivenParallelism() = runTest {
        val channel = receiveChannelOf(1, 2, 3, 4, 5, 6).identityDelayedOperation(2, 500)

        val time = measureTimeMillis { channel.count() }

        assertTrue(time >= 1400, "should take more than 1500 ms but took only $time ms")
    }
}
