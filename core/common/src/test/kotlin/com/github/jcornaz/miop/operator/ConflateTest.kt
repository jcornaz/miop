package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.conflate
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

class ConflateTest : OperatorTest() {
    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> = conflate()

    @Test
    fun shouldConflate() = runTest {
        val source = Channel<Int>()

        val result = source.conflate()

        withTimeout(1000) {
            source.send(1)
            source.send(2)
            source.send(3)
            assertEquals(3, result.receive())
            source.send(4)
            assertEquals(4, result.receive())
        }
    }
}