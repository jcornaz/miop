package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.assertThrows
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ClosedReceiveChannelException
import kotlinx.coroutines.experimental.channels.first
import kotlinx.coroutines.experimental.launch
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscribableValueTest : AsyncTest() {

    @Test
    fun openSubscriptionShouldBeClosedOnceTheValueSent() = runTest {
        expect(1)
        val subscribable = SubscribableValue(42)
        assertEquals(42, subscribable.get())
        launch(Unconfined) {
            expect(2)
            val sub = subscribable.openSubscription()
            assertEquals(42, sub.receive())
            expect(3)
            assertThrows<ClosedReceiveChannelException> { sub.receive() }
            expect(4)
        }
        finish(5)
    }

    @Test
    fun openSubscriptionShouldAlwaysReturnTheGivenValue() {
        expect(1)
        val subscribable = SubscribableValue(42)
        launch(Unconfined) {
            expect(2)
            assertEquals(42, subscribable.openSubscription().first())
            assertEquals(42, subscribable.openSubscription().first())
            expect(3)
        }
        finish(4)
    }
}
