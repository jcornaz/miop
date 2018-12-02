package com.github.jcornaz.miop.operator.terminal

import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.failedReceiveChannel
import com.github.jcornaz.miop.operator.DummyException
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.sendTo
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SendToTest {

    @Test
    fun shouldSendElementToTarget() = runTest {
        val target = Channel<Int>(Channel.UNLIMITED)

        receiveChannelOf(1, 2, 3, 4).sendTo(target)
        target.close()

        assertEquals(listOf(1, 2, 3, 4), target.toList())
    }

    @Test
    fun shouldThrowUpstreamError() = runTest {
        val target = Channel<Int>(Channel.UNLIMITED)

        val exception = assertThrows<DummyException> {
            failedReceiveChannel<Int>(DummyException("my exception")).sendTo(target)
        }

        assertEquals("my exception", exception.message)
    }

    @Test
    fun shouldNotTransmitErrorToTarget() = runTest {
        val target = Channel<Int>(Channel.UNLIMITED)

        assertThrows<DummyException> {
            failedReceiveChannel<Int>(DummyException("my exception")).sendTo(target)
        }

        assertFalse(target.isClosedForSend)

        target.send(0)
        target.close()

        assertEquals(listOf(0), target.toList())
    }

    @Test
    fun shouldNotCloseTarget() = runTest {
        val target = Channel<Int>(Channel.UNLIMITED)

        emptyReceiveChannel<Int>().sendTo(target)

        assertFalse(target.isClosedForSend)

        target.send(0)
        target.close()

        assertEquals(listOf(0), target.toList())
    }
}
