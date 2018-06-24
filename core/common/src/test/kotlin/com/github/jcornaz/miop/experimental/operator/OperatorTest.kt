package com.github.jcornaz.miop.experimental.operator

import com.github.jcornaz.miop.internal.test.assertThrows
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class OperatorTest {

    abstract fun ReceiveChannel<Int>.operator(): ReceiveChannel<Int>

    @Test
    fun testCancel() = runTest {
        val source = Channel<Int>()
        source.operator().cancel(DummyException("something went wrong"))

        val exception = assertThrows<DummyException> { source.receive() }
        assertEquals("something went wrong", exception.message)
    }
}