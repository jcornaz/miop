package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import com.github.jcornaz.miop.transform
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

class TransformOperatorTest : OperatorTest() {
    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> =
        transform { input, output -> input.consumeEach { output.send(it) } }

    @Test
    fun testSimpleTransform() = runTest {
        val result: ReceiveChannel<String> = receiveChannelOf(1, 2, 3).transform(coroutineContext) { input, output ->
            assertEquals(listOf(1, 2, 3), input.toList())

            output.send("hello")
            output.send("world")
        }

        assertEquals(listOf("hello", "world"), result.toList())
    }

    @Test
    fun testFailingTransform() = runTest {
        val source = Channel<Int>()

        val result: ReceiveChannel<String> = source.transform { _, _ -> throw DummyException("something went wrong") }

        assertThrows<Exception> { source.receive() }

        val e2 = assertThrows<DummyException> { result.receive() }
        assertEquals("something went wrong", e2.message)
    }

    @Test
    fun shouldSendCloseToken() = runTest {
        val result = emptyReceiveChannel<Int>().transform<Int, Int> { _, _ -> }

        withTimeout(1000) {
            assertThrows<ClosedReceiveChannelException> { result.receive() }
        }
    }

    @Test
    fun shouldWaitForChildCoroutine() = runTest {
        val channel = produce {
            delay(300)
            send(1)
            delay(300)
            send(2)
        }

        val result = channel.transform<Int, Int> { input, output ->
            launch {
                delay(400)
                output.send(input.receive())
                launch {
                    delay(400)
                    output.send(input.receive())
                }
            }
        }

        assertEquals(listOf(1, 2), result.toList())
    }
}
