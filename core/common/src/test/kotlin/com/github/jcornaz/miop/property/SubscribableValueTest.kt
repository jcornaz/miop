package com.github.jcornaz.miop.property

import com.github.jcornaz.miop.test.AsyncTest
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscribableValueTest : AsyncTest() {

    @Test
    fun openSubscriptionShouldBeClosedOnceTheValueSent() = runTest {
        expect(1)
        val subscribable = SubscribableValue(42)
        assertEquals(42, subscribable.get())
        launch(Dispatchers.Unconfined) {
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
    fun openSubscriptionShouldAlwaysReturnTheGivenValue() = runTest {
        expect(1)
        val subscribable = SubscribableValue(42)
        launch(Dispatchers.Unconfined) {
            expect(2)
            assertEquals(42, subscribable.openSubscription().first())
            assertEquals(42, subscribable.openSubscription().first())
            expect(3)
        }
        finish(4)
    }
}
