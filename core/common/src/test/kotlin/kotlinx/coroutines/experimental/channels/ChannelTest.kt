package kotlinx.coroutines.channels

import com.github.jcornaz.miop.experimental.operator.DummyException
import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.assertThrows
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Unconfined
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Theses are assumption of the channels in kotlinx.coroutines.
 */
class ChannelTest : AsyncTest() {

    @Test
    fun testCancelledChannel() = runTest {
        val channel = Channel<Int>().apply { cancel() }

        assertThrows<ClosedReceiveChannelException> { channel.receive() }
        assertThrows<ClosedSendChannelException> { channel.send(0) }
    }

    @Test
    fun testFailedChannel() = runTest {
        val channel = Channel<Int>().apply { cancel(DummyException("something went wrong")) }

        val e1 = assertThrows<DummyException> { channel.receive() }
        assertEquals("something went wrong", e1.message)

        val e2 = assertThrows<DummyException> { channel.send(0) }
        assertEquals("something went wrong", e2.message)
    }

    @Test
    fun testCancellingProduce() {
        var onCompletionCalled = false
        produce<Int>(Unconfined, onCompletion = { onCompletionCalled = true }) { }.cancel()
        assertTrue(onCompletionCalled)
    }

    @Test
    fun testCancelReceive() = runTest {
        val channel = Channel<Int>()

        expect(1)
        val job = launch(Unconfined) {
            expect(2)
            try {
                channel.receive()
            } catch (t: CancellationException) {
                expect(4)
            }
        }

        expect(3)
        job.cancelAndJoin()
        finish(5)
    }

    @Test
    fun testCancelProduce() = runTest {
        val source = Channel<Int>()

        val produced = produce<Int>(Unconfined, onCompletion = source.consumes()) {
            source.receive()
        }

        produced.cancel()

        assertThrows<Exception> { source.receive() }
    }
}
