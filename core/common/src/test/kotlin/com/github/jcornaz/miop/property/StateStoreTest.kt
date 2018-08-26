package com.github.jcornaz.miop.property

import com.github.jcornaz.miop.test.AsyncTest
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.timeunit.TimeUnit
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

open class StateStoreTest : AsyncTest() {

    open fun <S> createStore(initialState: S): StateStore<S, (S) -> S> = StateStore(initialState)

    @Test
    fun dispatchShouldMutateTheState() = runTest {
        val store = createStore(-1)

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
        withTimeout(1, TimeUnit.SECONDS) { barrier.withLock { } }
        expect(5)

        assertEquals(42, store.get())

        finish(6)
    }

    @Test
    fun handleShouldReturnTheNewState() = runTest {
        val store = createStore(0)

        assertEquals(42, store.handle { it + 42 })
        assertEquals(42, store.get())
    }

    @Test
    fun subscriptionShouldAlwaysImmediatelyStartWithCurrentState() = runTest {
        val store = createStore("Hello world")

        withTimeout(1, TimeUnit.SECONDS) {
            assertEquals("Hello world", store.get())
            assertEquals("Hello world", store.openSubscription().first())
            assertEquals("Hello world", store.openSubscription().first())
            assertEquals("Hello world", store.openSubscription().first())
        }
    }

    @Test
    fun subscriptionShouldNotReceiveUnchangedState() = runTest {
        val store = createStore("Hello")

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
        withTimeout(1, TimeUnit.SECONDS) { barrier.withLock { } }
        finish(6)
    }
}
