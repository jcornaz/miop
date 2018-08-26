package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.buffer
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

class BufferTest : OperatorTest() {
    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> = buffer()

    @Test
    fun shouldNotSuspendWithEmptyBuffer() = runTest {
        val source = Channel<Int>()

        val result = source.buffer(3)

        withTimeout(1000) {
            repeat(3) { source.send(it) }
        }

        assertEquals((0 until 3).toList(), result.toList())
    }
}
