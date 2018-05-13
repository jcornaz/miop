package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.AsyncTest
import com.github.jcornaz.miop.experimental.assertThrows
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ClosedReceiveChannelException
import kotlinx.coroutines.experimental.channels.first
import kotlinx.coroutines.experimental.launch
import org.junit.Test
import kotlin.test.assertEquals

class SubscribableValueTest : AsyncTest() {

    @Test
    fun `openSubscription should be closed once the value sent`() {
        expect(1)
        val subscribable = SubscribableValue(42)
        val value by subscribable
        assertEquals(42, subscribable.value)
        assertEquals(42, value)
        launch(Unconfined) {
            expect(2)
            val sub = subscribable.openValueSubscription()
            assertEquals(42, sub.receive())
            expect(3)
            assertThrows<ClosedReceiveChannelException> { sub.receive() }
            expect(4)
        }
        finish(5)
    }

    @Test
    fun `openSubscription should always return the given value`() {
        expect(1)
        val subscribable = SubscribableValue(42)
        launch(Unconfined) {
            expect(2)
            assertEquals(42, subscribable.openValueSubscription().first())
            assertEquals(42, subscribable.openValueSubscription().first())
            expect(3)
        }
        finish(4)
    }
}
