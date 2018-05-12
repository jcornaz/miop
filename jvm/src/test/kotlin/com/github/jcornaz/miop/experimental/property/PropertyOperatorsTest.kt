package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.AsyncTest
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.channels.toList
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.yield
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyOperatorsTest : AsyncTest() {

    @Test(timeout = 1000)
    fun `combining constants should return a subscribable constant`() = runBlocking {
        val subscribable = SubscribableValue(6).combineWith(SubscribableValue(9)) { x, y -> x * y }
        assertEquals(54, subscribable.value)
        assertEquals(listOf(54), subscribable.openSubscription().toList())
        assertEquals(listOf(54), subscribable.openSubscription().toList())
    }

    @Test
    fun `updating the source of a combined subcribable should update the combined value`() = runBlocking {
        val x = SubscribableVariable(1)
        val y = SubscribableVariable(1)
        val result = x.combineWith(y) { vx, vy -> vx * vy }

        assertEquals(1, result.value)

        expect(1)
        launch(coroutineContext, CoroutineStart.UNDISPATCHED) {
            expect(2)
            val sub = result.openSubscription()
            assertEquals(1, sub.receive())
            expect(3)
            assertEquals(6, sub.receive()) // suspend
            expect(6)
            assertEquals(54, sub.receive()) // suspend
            expect(9)
        }

        expect(4)
        x.value = 6
        assertEquals(6, result.value)
        expect(5)
        yield() // to child
        expect(7)
        y.value = 9
        assertEquals(54, result.value)
        expect(8)
        yield() // to child
        finish(10)
    }

    @Test(timeout = 1000)
    fun `mapping a subscribable constant should return a subscribable constant`() = runBlocking {
        val subscribable = SubscribableValue(21).map { it * 2 }
        assertEquals(42, subscribable.value)
        assertEquals(listOf(42), subscribable.openSubscription().toList())
        assertEquals(listOf(42), subscribable.openSubscription().toList())
    }

    @Test
    fun `updating the source of a mapped subcribable should update the mapped value`() = runBlocking {
        val x = SubscribableVariable(1)
        val result = x.map { it * 2 }

        assertEquals(2, result.value)

        expect(1)
        launch(coroutineContext, CoroutineStart.UNDISPATCHED) {
            expect(2)
            val sub = result.openSubscription()
            assertEquals(2, sub.receive())
            expect(3)
            assertEquals(4, sub.receive()) // suspend
            expect(6)
            assertEquals(6, sub.receive()) // suspend
            expect(9)
        }

        expect(4)
        x.value = 2
        assertEquals(4, result.value)
        expect(5)
        yield() // to child
        expect(7)
        x.value = 3
        assertEquals(6, result.value)
        expect(8)
        yield() // to child
        finish(10)
    }
}
