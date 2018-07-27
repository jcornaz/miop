package com.github.jcornaz.miop.experimental.operator

import com.github.jcornaz.miop.experimental.receiveChannelOf
import com.github.jcornaz.miop.experimental.transform
import com.github.jcornaz.miop.internal.test.assertThrows
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.toList
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

class FreeFormOperatorTest : OperatorTest() {
    override fun ReceiveChannel<Int>.operator(): ReceiveChannel<Int> =
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

        val e1 = assertThrows<DummyException> { source.receive() }
        assertEquals("something went wrong", e1.message)

        val e2 = assertThrows<DummyException> { result.receive() }
        assertEquals("something went wrong", e2.message)
    }

}
