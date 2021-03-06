package com.github.jcornaz.miop.property

import com.github.jcornaz.miop.test.AsyncTest
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscribableVariableTest : AsyncTest() {

    @Test
    fun basicScenario() = runTest {
        expect(1)
        val subscribable = SubscribableVariable(42)
        assertEquals(42, subscribable.get())
        launch(coroutineContext, start = CoroutineStart.UNDISPATCHED) {
            expect(2)
            val sub = subscribable.openSubscription()
            assertEquals(42, sub.receive())
            expect(3)
            assertEquals(24, sub.receive()) // suspend
            expect(6)
        }
        expect(4)
        subscribable.set(24)
        assertEquals(24, subscribable.get())
        expect(5)
        yield() // to child
        finish(7)
    }

    @Test
    fun openSubscriptionShouldAlwaysStartWithTheCurrentValue() = runTest {
        expect(1)
        val subscribable = SubscribableVariable(42)
        launch(Dispatchers.Unconfined) {
            expect(2)
            assertEquals(42, subscribable.openSubscription().first())
            assertEquals(42, subscribable.openSubscription().first())
            expect(3)
        }
        finish(4)
    }
}
