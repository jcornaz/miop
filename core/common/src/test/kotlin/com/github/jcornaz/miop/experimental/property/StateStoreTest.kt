package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.take
import kotlinx.coroutines.experimental.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class StateStoreTest : AsyncTest() {

    @Test
    fun dispatchShouldMutateTheState() = runTest {
        val store = StateStore(-1)

        assertEquals(-1, store.get())
        val result = async(Unconfined) { store.openSubscription().take(2).toList() }

        store.dispatch { assertEquals(-1, it); 42 }

        assertEquals(listOf(-1, 42), result.await())
        assertEquals(42, store.get())
    }
}
