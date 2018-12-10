package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.failedReceiveChannel
import com.github.jcornaz.miop.mergeWith
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MergeTest : OperatorTest() {
    override fun <T : Any> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        mergeWith(emptyReceiveChannel())


    @Test
    fun shouldGiveTheElementsAsSoonAsReceived() = runTest {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.mergeWith(source2)

        expect(1)
        launch(Dispatchers.Unconfined) {
            expect(2)
            assertEquals(1, result.receive())
            expect(4)
            assertEquals(2, result.receive())
            expect(6)
            assertEquals(3, result.receive())
            expect(8)
            assertEquals(4, result.receive())
            expect(10)
            assertEquals(5, result.receive())
            expect(12)
            assertEquals(6, result.receive())
            expect(14)
            assertThrows<ClosedReceiveChannelException> { result.receive() }
            expect(16)
        }
        expect(3)
        source1.send(1)
        expect(5)
        source1.send(2)
        expect(7)
        source2.send(3)
        expect(9)
        source2.send(4)
        expect(11)
        source1.send(5)
        source1.close()
        expect(13)
        source2.send(6)
        expect(15)
        source2.close()
        finish(17)
    }

    @Test
    fun cancellingASourceShouldCancelAllTheSources() = runTest {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.mergeWith(source2)

        result.cancel()

        assertTrue(source1.isClosedForReceive)
        assertTrue(source2.isClosedForReceive)

        assertThrows<Exception> { source1.receive() }
        assertThrows<Exception> { source2.receive() }
    }

    @Test
    fun ifASourceFailsTheOtherSourcesShouldBeCancelled() = runTest {
        val source1 = Channel<Int>()
        val source2 = failedReceiveChannel<Int>(Exception("my exception"))
        val result = source1.mergeWith(source2)

        assertTrue(source1.isClosedForReceive, "other source should be closed for receive")
        assertTrue(result.isClosedForReceive, "result should be closed for receive")

        assertThrows<Exception> { source1.receive() }
        assertEquals("my exception", assertThrows<Exception> { result.receive() }.message)
    }

    @Test
    fun itShouldBePossibleToUseMergeWithFiniteChannel() = runTest {
        val result = receiveChannelOf(1).mergeWith(receiveChannelOf(2))

        assertEquals(1, result.receive())
        assertEquals(2, result.receive())
        assertThrows<ClosedReceiveChannelException> { result.receive() }
    }
}
