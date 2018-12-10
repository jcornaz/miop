package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.combineLatestWith
import com.github.jcornaz.miop.failedReceiveChannel
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

class CombineLatestTest : OperatorTest() {

    override fun <T : Any> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        combineLatestWith(receiveChannelOf(1)) { it, _ -> it }

    @Test
    fun combineLatestShouldGiveTheCombinedElements() = runTest {
        val source1 = Channel<Int>()
        val source2 = Channel<Int>()
        val result = source1.combineLatestWith(source2) { v1, v2 -> v1 to v2 }

        expect(1)
        launch(Dispatchers.Unconfined) {
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
        val source2 = failedReceiveChannel<Int>(Exception("my exception"))
        val result = source1.combineLatestWith(source2) { v1, v2 -> v1 to v2 }

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
}