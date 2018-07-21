package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.toList
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.timeunit.TimeUnit
import kotlinx.coroutines.experimental.withTimeout
import kotlinx.coroutines.experimental.yield
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyOperatorsTest : AsyncTest() {

    @Test
    fun combiningConstantsShouldReturnASubscribableConstant() = runTest {
        val subscribable = SubscribableValue(6).combineWith(SubscribableValue(9)) { x, y -> x * y }
        assertEquals(54, subscribable.get())
        assertEquals(listOf(54), subscribable.openSubscription().toList())
        assertEquals(listOf(54), subscribable.openSubscription().toList())
    }

    @Test
    fun updatingTheSourceOfACombinedSubcribableShouldUpdateTheCombinedValue() = runTest {
        val x = SubscribableVariable(1)
        val y = SubscribableVariable(1)
        val result = x.combineWith(y) { vx, vy -> vx * vy }

        assertEquals(1, result.get())

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
        x.set(6)
        assertEquals(6, result.get())
        expect(5)
        yield() // to child
        expect(7)
        y.set(9)
        assertEquals(54, result.get())
        expect(8)
        yield() // to child
        finish(10)
    }

    @Test
    fun mappingASubscribableConstantShouldReturnASubscribableConstant() = runTest {
        val subscribable = SubscribableValue(21).map { it * 2 }
        assertEquals(42, subscribable.get())
        assertEquals(listOf(42), subscribable.openSubscription().toList())
        assertEquals(listOf(42), subscribable.openSubscription().toList())
    }

    @Test
    fun updatingSourceOfMappedSubscribableShouldUpdateTheMappedValue() = runTest {
        val x = SubscribableVariable(1)
        val result = x.map { it * 2 }

        assertEquals(2, result.get())

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
        x.set(2)
        assertEquals(4, result.get())
        expect(5)
        yield() // to child
        expect(7)
        x.set(3)
        assertEquals(6, result.get())
        expect(8)
        yield() // to child
        finish(10)
    }

    @Test
    fun switchMapShouldAlwaysReflectTheLatestResultOfTransform() = runTest {
        val source1 = SubscribableValue(1)
        val source2 = SubscribableVariable(2)
        val switch = SubscribableVariable(0)
        val result = switch.switchMap { listOf(source1, source2)[it] }

        expect(1)
        launch(coroutineContext, CoroutineStart.UNDISPATCHED) {
            expect(2)
            val sub = result.openSubscription()
            assertEquals(1, sub.receive())
            expect(3)
            assertEquals(2, sub.receive()) // suspend
            expect(6)
            assertEquals(42, sub.receive()) // suspend
            expect(9)
            assertEquals(1, sub.receive()) // suspend
            expect(12)
            assertEquals(42, sub.receive()) // suspend
            expect(15)
        }

        expect(4)
        assertEquals(1, result.get())
        switch.set(1)
        assertEquals(2, result.get())
        expect(5)
        yield() // to child
        expect(7)
        source2.set(42)
        assertEquals(42, result.get())
        expect(8)
        yield() // to child
        expect(10)
        switch.set(0)
        assertEquals(1, result.get())
        expect(11)
        yield() // to child
        expect(13)
        switch.set(1)
        assertEquals(42, result.get())
        expect(14)
        yield() // to child
        finish(16)
    }

    @Test
    fun openMappedSubscriptionShouldMapElements() = runTest {
        val source = SubscribableVariable(1)
        val result: ReceiveChannel<String> = source.openSubscription { (it * 2).toString() }

        expect(1)
        launch(coroutineContext) {
            expect(2)
            assertEquals("2", result.receive())
            expect(3)
            assertEquals("4", result.receive()) // suspend
            expect(5)
        }

        expect(4)
        source.set(2)
        yield() // to child
        finish(6)
    }

    @Test
    fun openMappedSubscriptionShouldNotEmitTheSameReferenceTwice() = runTest {
        val ref = "less than ten"
        val source = SubscribableVariable(0)
        val result = source.openSubscription { if (it < 10) ref else it.toString() }

        expect(1)
        launch(coroutineContext) {
            expect(2)
            assertEquals("less than ten", result.receive())
            expect(3)
            assertEquals("12", result.receive()) // suspend
            expect(6)
        }

        expect(4)
        source.set(2)
        yield()
        expect(5)
        source.set(12)
        yield() // to child
        finish(7)
    }

    @Test
    fun openMappedSubscriptionShouldSendCloseToken() = runTest {
        withTimeout(1, TimeUnit.SECONDS) {
            assertEquals(listOf("42"), SubscribableValue(42).openSubscription { it.toString() }.toList())
        }
    }
}
