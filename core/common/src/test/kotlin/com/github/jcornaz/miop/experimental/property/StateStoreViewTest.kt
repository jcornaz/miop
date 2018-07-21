package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

private val NUMBERS = listOf("zero", "one", "two", "three")

class StateStoreViewTest : StateStoreTest() {
    override fun <S> createStore(initialState: S): StateStore<S, (S) -> S> =
        StateStore(initialState).map(transformState = { it }, transformAction = { it })

    @Test
    fun shouldMapState() = runTest {
        val strings = StateStore("one")
        val values = strings.map(
            transformState = { NUMBERS.indexOf(it) },
            transformAction = { action: (Int) -> Int -> { NUMBERS[action(NUMBERS.indexOf(it))] } }
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

        values.dispatch { it + 1 }
        job1.join()
        job2.join()

        assertEquals("two", strings.get())
        assertEquals(2, values.get())
    }
}
