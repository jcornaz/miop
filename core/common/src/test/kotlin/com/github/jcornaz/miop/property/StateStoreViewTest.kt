package com.github.jcornaz.miop.property

import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

private val NUMBERS = listOf("zero", "one", "two", "three")

class StateStoreViewTest : StateStoreTest() {

    override fun <S> createStore(initialState: S): StateStore<S, (S) -> S> =
        StateStore(initialState).map(transformState = { it }, transformEvent = { it })

    @Test
    fun shouldMapState() = runTest {
        val strings = StateStore("one")
        val values = strings.map(
            transformState = { NUMBERS.indexOf(it) },
            transformEvent = { event: (Int) -> Int -> { NUMBERS[event(NUMBERS.indexOf(it))] } }
        )

        assertEquals("one", strings.get())
        assertEquals(1, values.get())

        val job1 = launch(coroutineContext, start = CoroutineStart.UNDISPATCHED) {
            strings.openSubscription().consume {
                assertEquals("one", receive())
                assertEquals("two", receive())
            }
        }

        val job2 = launch(coroutineContext, start = CoroutineStart.UNDISPATCHED) {
            values.openSubscription().consume {
                assertEquals(1, receive())
                assertEquals(2, receive())
            }
        }

        values.handle { it + 1 }

        withTimeout(1000) {
            job1.join()
            job2.join()
        }

        assertEquals("two", strings.get())
        assertEquals(2, values.get())
    }
}
