package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.first
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import kotlinx.coroutines.experimental.timeunit.TimeUnit
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

class StateStoreTest : AsyncTest() {

    @Test
    fun dispatchShouldMutateTheState() = runTest {
        val store = StateStore(-1)

        assertEquals(-1, store.get())

        val barrier = Mutex(true)

        expect(1)
        launch(coroutineContext, start = CoroutineStart.UNDISPATCHED) {
            val sub = store.openSubscription()
            assertEquals(-1, sub.receive())

            expect(2)

            assertEquals(42, sub.receive()) // suspend
            expect(4)
            barrier.unlock()
        }

        expect(3)

        store.dispatch { assertEquals(-1, it); 42 }
        withTimeout(1, TimeUnit.SECONDS) { barrier.withLock {  } }
        expect(5)

        assertEquals(42, store.get())

        finish(6)
    }

    @Test
    fun subscriptionShouldAlwaysImmediatelyStartWithCurrentState() = runTest {
        val store = StateStore("Hello world")

        withTimeout(1, TimeUnit.SECONDS) {
            assertEquals("Hello world", store.get())
            assertEquals("Hello world", store.openSubscription().first())
            assertEquals("Hello world", store.openSubscription().first())
            assertEquals("Hello world", store.openSubscription().first())
        }
    }

    @Test
    fun subscriptionShouldNotReceiveUnchangedState() = runTest {
        val store = StateStore("Hello")

        val barrier = Mutex(true)

        expect(1)
        launch(coroutineContext, start = CoroutineStart.UNDISPATCHED) {
            val sub = store.openSubscription()
            assertEquals("Hello", sub.receive())
            expect(2)
            assertEquals("World", sub.receive()) // suspend
            expect(5)
            barrier.unlock()
        }

        expect(3)
        store.dispatch { it } // should not resume the job
        delay(500, TimeUnit.MILLISECONDS)
        expect(4)

        store.dispatch { "World" } // should resume the job
        withTimeout(1, TimeUnit.SECONDS) { barrier.withLock {  } }
        finish(6)
    }
}
