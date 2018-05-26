package com.github.jcornaz.miop.experimental

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.assertThrows
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.mockito.Mockito
import org.mockito.verification.VerificationMode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OperatorsTest : AsyncTest() {

    @Test
    fun `merge should give the elements as soon as received`() = runBlocking {
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
    fun `cancelling a channel merged by mergeWith should cancel all the sources`() = runBlocking<Unit> {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.mergeWith(source2)

        result.cancel()

        assertTrue(source1.isClosedForReceive)
        assertTrue(source2.isClosedForReceive)

        assertThrows<ClosedReceiveChannelException> { source1.receive() }
        assertThrows<ClosedReceiveChannelException> { source2.receive() }
    }

    @Test
    fun `if a source merged by mergeWith fails, the result should fail and the other sources should be cancelled`() = runBlocking {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.mergeWith(source2)

        source2.cancel(Exception("my exception"))

        assertTrue(source1.isClosedForReceive, "other source should be closed for receive")
        assertTrue(result.isClosedForReceive, "result should be closed for receive")

        assertThrows<ClosedReceiveChannelException> { source1.receive() }
        assertEquals("my exception", assertThrows<Exception> { result.receive() }.message)
    }

    @Test
    fun `it should be possible to use merge with finite channel`() = runBlocking<Unit> {
        val result = receiveChannelOf(1).mergeWith(receiveChannelOf(2))

        assertEquals(1, result.receive())
        assertEquals(2, result.receive())
        assertThrows<ClosedReceiveChannelException> { result.receive() }
    }

    @Test
    fun `combine latest should give the combined elements`() = runBlocking {
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
    fun `cancelling a channel merged by combineLatest should cancel all the sources`() = runBlocking<Unit> {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.combineLatestWith(source2) { v1, v2 -> v1 to v2 }

        result.cancel()

        assertTrue(source1.isClosedForReceive)
        assertTrue(source2.isClosedForReceive)

        assertThrows<ClosedReceiveChannelException> { source1.receive() }
        assertThrows<ClosedReceiveChannelException> { source2.receive() }
    }

    @Test
    fun `if a source merged by combineLatest fails, the result should fail and the other sources should be cancelled`() = runBlocking {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.combineLatestWith(source2) { v1, v2 -> v1 to v2 }

        source2.cancel(Exception("my exception"))

        assertTrue(source1.isClosedForReceive, "other source should be closed for receive")
        assertTrue(result.isClosedForReceive, "result should be closed for receive")

        assertThrows<ClosedReceiveChannelException> { source1.receive() }
        assertEquals("my exception", assertThrows<Exception> { result.receive() }.message)
    }

    @Test
    fun `it should be possible to use combine latest with finite channel`() = runBlocking<Unit> {
        val result = receiveChannelOf(1).combineLatestWith(receiveChannelOf('a')) { v1, v2 -> v1 to v2 }

        assertEquals(1 to 'a', result.receive())
        assertThrows<ClosedReceiveChannelException> { result.receive() }
    }

    @Test
    fun `switchMap should emit items of the new source only`() = runBlocking {
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
        assertThrows<ClosedSendChannelException> { sources[0].send('x') } // the first source should have been cancelled
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
    fun `switchMap on an empty channel should return an empty channel`() = runBlocking<Unit> {
        val result = emptyReceiveChannel<Int>().switchMap { receiveChannelOf(1, 2, 3) }

        assertTrue(result.isClosedForReceive)
        assertThrows<ClosedReceiveChannelException> { result.receive() }
    }

    @Test
    fun `cancelling a channel created by mergeMap should cancel the current source`() = runBlocking<Unit> {
        val source = Channel<Int>()
        val result = receiveChannelOf(1).switchMap { source }

        result.cancel()

        assertTrue(source.isClosedForReceive)
        assertThrows<ClosedReceiveChannelException> { source.receive() }
    }

    @Test
    fun `if a source returned by switchMap fails, the result should fail with the same exception`() = runBlocking {
        val source = Channel<Int>()
        val result = receiveChannelOf(1).switchMap { source }

        source.close(Exception("my exception"))

        assertTrue(result.isClosedForReceive)
        assertEquals("my exception", assertThrows<Exception> { result.receive() }.message)
    }

    @Test
    fun `launchConsumeEach should consume the channel`() = runBlocking {
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
    fun `distinctUntilChanged should not send twice the same value in a row`() = runBlocking {
        val receivedValues = receiveChannelOf(1, 2, 2, 2, 3, 2, 1, 1).distinctUntilChanged().toList()

        assertEquals(listOf(1, 2, 3, 2, 1), receivedValues)
    }

    @Test
    fun `distinctUntilChanged should emit the upstream error if any`() = runBlocking {
        val exception = assertThrows<Exception> { produce<Int> { throw Exception("my exception") }.distinctUntilChanged().first() }
        assertEquals("my exception", exception.message)
    }

    @Test
    fun `cancelling the result of distinctUntilChanged should cancel the upstream channel`() = runBlocking<Unit> {
        val source = mock<ReceiveChannel<Int>>()
        val result = source.distinctUntilChanged()
        verify(source, never()).cancel()
        result.cancel()
        verify(source, atLeastOnce()).cancel()
    }
}
