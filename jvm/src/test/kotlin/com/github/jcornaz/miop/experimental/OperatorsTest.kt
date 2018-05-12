package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ClosedReceiveChannelException
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
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

        // The exception should not be delivered to the uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler { _, _ -> unreachable() }

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

        // The exception should not be delivered to the uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler { _, _ -> unreachable() }

        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.combineLatestWith(source2) { v1, v2 -> v1 to v2 }

        source2.cancel(Exception("my exception"))

        assertTrue(source1.isClosedForReceive, "other source should be closed for receive")
        assertTrue(result.isClosedForReceive, "result should be closed for receive")

        assertThrows<ClosedReceiveChannelException> { source1.receive() }
        assertEquals("my exception", assertThrows<Exception> { result.receive() }.message)
    }
}
