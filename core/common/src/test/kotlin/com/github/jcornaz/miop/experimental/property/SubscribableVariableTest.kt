package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.first
import org.junit.Test
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.assertEquals

class SubscribableVariableTest : AsyncTest() {

    @Test
    fun `basic scenario`() = runTest {
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
    fun `openSubscription should always start with the current value`() {
        expect(1)
        val subscribable = SubscribableVariable(42)
        launch(Unconfined) {
            expect(2)
            assertEquals(42, subscribable.openSubscription().first())
            assertEquals(42, subscribable.openSubscription().first())
            expect(3)
        }
        finish(4)
    }
}
