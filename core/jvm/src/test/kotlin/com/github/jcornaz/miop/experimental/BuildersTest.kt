package com.github.jcornaz.miop.experimental

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.assertThrows
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.ClosedReceiveChannelException
import kotlin.test.*

class BuildersTest : AsyncTest() {

    @Test
    fun `emptyReceiveChannel should return a channel already closed`() = runBlocking<Unit> {
        val channel = emptyReceiveChannel<Int>()

        assertFalse(channel.isEmpty)
        assertTrue(channel.isClosedForReceive)
        assertThrows<ClosedReceiveChannelException> { channel.receive() }
    }

    @Test
    fun `Many invocation of emptyReceiveChannel should return the same instance`() {
        assertSame<Any>(emptyReceiveChannel<Int>(), emptyReceiveChannel<String>())
    }

    @Test
    fun `receiveChannelOf should return a channel containing the given elements`() {
        val channel = receiveChannelOf(1, 2, 3)

        assertFalse(channel.isEmpty)
        assertFalse(channel.isClosedForReceive)

        expect(1)
        launch(Unconfined) {
            expect(2)
            assertEquals(1, channel.receive())
            assertEquals(2, channel.receive())
            assertEquals(3, channel.receive())
            assertThrows<ClosedReceiveChannelException> { channel.receive() }
            expect(3)
        }
        finish(4)
    }

    @Test
    fun `receiveChannelOf should return an already closed channel if no element is given`() = runBlocking<Unit> {
        val channel = receiveChannelOf<Int>()

        assertFalse(channel.isEmpty)
        assertTrue(channel.isClosedForReceive)
        assertThrows<ClosedReceiveChannelException> { channel.receive() }
    }

    @Test
    fun `Iterable#openSubscription should emits all elements of the iterable`() {
        val channel = listOf(1, 2, 3).openSubscription(capacity = 3)

        expect(1)
        launch(Unconfined) {
            expect(2)
            assertEquals(1, channel.receive())
            assertEquals(2, channel.receive())
            assertEquals(3, channel.receive())
            expect(3)
        }
        finish(4)
    }

    @Test
    fun `Sequence#openSubscription should emits all elements of the iterable`() {
        val channel = sequenceOf(1, 2, 3).openSubscription(capacity = 3)

        expect(1)
        launch(Unconfined) {
            expect(2)
            assertEquals(1, channel.receive())
            assertEquals(2, channel.receive())
            assertEquals(3, channel.receive())
            expect(3)
        }
        finish(4)
    }
}
