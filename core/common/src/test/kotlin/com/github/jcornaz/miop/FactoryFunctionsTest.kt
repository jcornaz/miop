package com.github.jcornaz.miop

import com.github.jcornaz.miop.test.AsyncTest
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.test.*

class FactoryFunctionsTest : AsyncTest() {

    @Test
    fun emptyReceiveChannelShouldReturnAChannelAlreadyClosed() = runTest {
        val channel = emptyReceiveChannel<Int>()

        assertFalse(channel.isEmpty)
        assertTrue(channel.isClosedForReceive)
        assertThrows<ClosedReceiveChannelException> { channel.receive() }
    }

    @Test
    fun manyInvocationOfEmptyReceiveChannelShouldReturnTheSameInstance() {
        assertSame<Any>(emptyReceiveChannel<Int>(), emptyReceiveChannel<String>())
    }

    @Test
    fun consumingFailedReceiveChannelShouldThrowTheGivenError() = runTest {
        val error = Exception("my exception")
        val channel = failedReceiveChannel<String>(error)

        val thrownError = assertThrows<Exception> { channel.consumeEach {  } }

        assertSame(error, thrownError)
    }

    @Test
    fun receiveChannelOfShouldReturnAChannelContainingTheGivenElements() = runTest {
        val channel = receiveChannelOf(1, 2, 3)

        assertFalse(channel.isEmpty)
        assertFalse(channel.isClosedForReceive)

        expect(1)
        launch(Dispatchers.Unconfined) {
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
    fun receiveChannelOfShouldReturnAnAlreadyClosedChannelIfNoElementIsGiven() = runTest {
        val channel = receiveChannelOf<Int>()

        assertFalse(channel.isEmpty)
        assertTrue(channel.isClosedForReceive)
        assertThrows<ClosedReceiveChannelException> { channel.receive() }
    }

    @Test
    fun openSubscriptionOnIterableShouldEmitsAllElementsOfTheIterable() = runTest {
        val channel = listOf(1, 2, 3).openSubscription(capacity = 3)

        expect(1)
        launch(Dispatchers.Unconfined) {
            expect(2)
            assertEquals(1, channel.receive())
            assertEquals(2, channel.receive())
            assertEquals(3, channel.receive())
            expect(3)
        }
        finish(4)
    }

    @Test
    fun openSubscriptionOnSequenceShouldEmitsAllElementsOfTheIterable() = runTest {
        val channel = sequenceOf(1, 2, 3).openSubscription(capacity = 3)

        expect(1)
        launch(Dispatchers.Unconfined) {
            expect(2)
            assertEquals(1, channel.receive())
            assertEquals(2, channel.receive())
            assertEquals(3, channel.receive())
            expect(3)
        }
        finish(4)
    }
}
