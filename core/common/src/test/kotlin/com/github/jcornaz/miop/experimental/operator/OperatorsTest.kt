package com.github.jcornaz.miop.experimental.operator

import com.github.jcornaz.miop.experimental.combineLatestWith
import com.github.jcornaz.miop.experimental.distinctUntilChanged
import com.github.jcornaz.miop.experimental.emptyReceiveChannel
import com.github.jcornaz.miop.experimental.launchConsumeEach
import com.github.jcornaz.miop.experimental.mergeWith
import com.github.jcornaz.miop.experimental.receiveChannelOf
import com.github.jcornaz.miop.experimental.switchMap
import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.assertThrows
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ClosedReceiveChannelException
import kotlinx.coroutines.experimental.channels.first
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.channels.toList
import kotlinx.coroutines.experimental.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OperatorsTest : AsyncTest() {

    @Test
    fun mergeShouldGiveTheElementsAsSoonAsReceived() = runTest {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.mergeWith(source2)

        expect(1)
        launch(Unconfined) {
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
    fun cancellingAChannelMergedByMergeWithShouldCancelAllTheSources() = runTest {
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
    fun ifASourceMergedByMergeWithFailsTheResultShouldFailAndTheOtherSourcesShouldBeCancelled() = runTest {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.mergeWith(source2)

        source2.cancel(Exception("my exception"))

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

    @Test
    fun combineLatestShouldGiveTheCombinedElements() = runTest {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.combineLatestWith(source2) { v1, v2 -> v1 to v2 }

        expect(1)
        launch(Unconfined) {
            expect(2)
            assertEquals(2 to 3, result.receive())
            expect(5)
            assertEquals(2 to 4, result.receive())
            expect(7)
            assertEquals(5 to 4, result.receive())
            expect(9)
            assertEquals(5 to 6, result.receive())
            expect(11)
            assertThrows<ClosedReceiveChannelException> { result.receive() }
            expect(13)
        }
        expect(3)
        source1.send(1)
        source1.send(2)
        expect(4)
        source2.send(3)
        expect(6)
        source2.send(4)
        expect(8)
        source1.send(5)
        source1.close()
        expect(10)
        source2.send(6)
        expect(12)
        source2.close()
        finish(14)
    }

    @Test
    fun cancellingAChannelMergedByCombineLatestShouldCancelAllTheSources() = runTest {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.combineLatestWith(source2) { v1, v2 -> v1 to v2 }

        result.cancel()

        assertTrue(source1.isClosedForReceive)
        assertTrue(source2.isClosedForReceive)

        assertThrows<Exception> { source1.receive() }
        assertThrows<Exception> { source2.receive() }
    }

    @Test
    fun ifASourceMergedByCombineLatestFailsTheResultShouldFailAndTheOtherSourcesShouldBeCancelled() = runTest {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.combineLatestWith(source2) { v1, v2 -> v1 to v2 }

        source2.cancel(Exception("my exception"))

        assertTrue(source1.isClosedForReceive, "other source should be closed for receive")
        assertTrue(result.isClosedForReceive, "result should be closed for receive")

        assertThrows<Exception> { source1.receive() }
        assertEquals("my exception", assertThrows<Exception> { result.receive() }.message)
    }

    @Test
    fun itShouldBePossibleToUseCombineLatestWithFiniteChannel() = runTest {
        val result = receiveChannelOf(1).combineLatestWith(receiveChannelOf('a')) { v1, v2 -> v1 to v2 }

        assertEquals(1 to 'a', result.receive())
        assertThrows<ClosedReceiveChannelException> { result.receive() }
    }

    @Test
    fun switchMapShouldEmitItemsOfTheNewSourceOnly() = runTest {
        val sources = (0..2).map { Channel<Char>(2) }
        val switch = Channel<Int>()
        val result = switch.switchMap { sources[it] }

        expect(1)
        launch(Unconfined) {
            expect(2)
            assertEquals('a', result.receive())
            assertEquals('b', result.receive())
            expect(5)
            assertEquals('c', result.receive())
            assertEquals('d', result.receive())
            expect(7)
            assertEquals('y', result.receive())
            expect(9)
            assertEquals('e', result.receive())
            assertEquals('f', result.receive())
            expect(11)
            assertThrows<ClosedReceiveChannelException> { result.receive() }
            expect(14)
        }

        expect(3)
        sources[0].send('a')
        sources[0].send('b')
        sources[1].send('c')
        sources[1].send('d')
        sources[2].send('e')
        sources[2].send('f')
        expect(4)
        switch.send(0) // start the first source
        expect(6)
        switch.send(1) // start the second source
        expect(8)
        assertThrows<Exception> { sources[0].send('x') } // the first source should have been cancelled
        sources[1].send('y')
        expect(10)
        sources[1].close() // should have no impact
        switch.send(2) // start the third source
        expect(12)
        switch.close() // should have no impact
        expect(13)
        sources[2].close() // should close the result as the switch is closed
        finish(15)
    }

    @Test
    fun switchMapOnAnEmptyChannelShouldReturnAnEmptyChannel() = runTest {
        val result = emptyReceiveChannel<Int>().switchMap { receiveChannelOf(1, 2, 3) }

        assertTrue(result.isClosedForReceive)
        assertThrows<Exception> { result.receive() }
    }

    @Test
    fun cancellingAChannelCreatedByMergeMapShouldCancelTheCurrentSource() = runTest {
        val source = Channel<Int>()
        val result = receiveChannelOf(1).switchMap { source }

        result.cancel()

        assertTrue(source.isClosedForReceive)
        assertThrows<Exception> { source.receive() }
    }

    @Test
    fun ifASourceReturnedBySwitchMapFailsTheResultShouldFailWithTheSameException() = runTest {
        val source = Channel<Int>()
        val result = receiveChannelOf(1).switchMap { source }

        source.close(Exception("my exception"))

        assertTrue(result.isClosedForReceive)
        assertEquals("my exception", assertThrows<Exception> { result.receive() }.message)
    }

    @Test
    fun launchConsumeEachShouldConsumeTheChannel() = runTest {
        val result = mutableListOf<Int>()

        val channel = receiveChannelOf(1, 2, 3)

        val job = channel.launchConsumeEach {
            result += it
        }

        job.join()

        assertEquals(listOf(1, 2, 3), result)

        assertTrue(channel.isClosedForReceive)
    }

    @Test
    fun distinctUntilChangedShouldNotSendTwiceTheSameValueInARow() = runTest {
        val receivedValues = receiveChannelOf(1, 2, 2, 2, 3, 2, 1, 1).distinctUntilChanged().toList()

        assertEquals(listOf(1, 2, 3, 2, 1), receivedValues)
    }

    @Test
    fun distinctUntilChangedShouldEmitTheUpstreamErrorIfAny() = runTest {
        val exception = assertThrows<Exception> { produce<Int> { throw Exception("my exception") }.distinctUntilChanged().first() }
        assertEquals("my exception", exception.message)
    }

    @Test
    fun cancellingTheResultOfDistinctUntilChangedShouldCancelTheUpstreamChannel() = runTest {
        val source = Channel<Int>()
        source.distinctUntilChanged().cancel(DummyException("something went wrong"))

        assertThrows<Exception> { source.send(0) }
    }
}
