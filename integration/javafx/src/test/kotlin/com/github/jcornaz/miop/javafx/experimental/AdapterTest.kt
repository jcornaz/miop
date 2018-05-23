package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.internal.test.AsyncTest
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.yield
import org.junit.Test
import kotlin.test.assertEquals

class AdapterTest : AsyncTest() {

    @Test
    fun `ObservableValue#asSubscribableValue() should return an adapter reflecting the observable`() = runBlocking {
        val property = SimpleIntegerProperty(1)
        val subscribable = property.asSubscribableValue()

        assertEquals(1, subscribable.get())

        expect(1)
        launch(Unconfined) {
            expect(2)
            val sub = subscribable.openSubscription()

            assertEquals(1, sub.receive())
            expect(3)
            assertEquals(2, sub.receive())
            expect(6)
        }

        expect(4)
        property.value = 2
        assertEquals(2, subscribable.get())
        expect(5)
        yield()
        finish(7)
    }

    @Test
    fun `Property#asSubscribableVariable() should return an adapter reflecting the reflect the property`() = runBlocking {
        val property = SimpleIntegerProperty(1)
        val subscribable = property.asSubscribableVariable()

        assertEquals(1, subscribable.get())

        expect(1)
        launch(Unconfined) {
            expect(2)
            val sub = subscribable.openSubscription()

            assertEquals(1, sub.receive())
            expect(3)
            assertEquals(2, sub.receive())
            expect(6)
            assertEquals(42, sub.receive())
            expect(9)
        }

        expect(4)
        property.value = 2
        assertEquals(2, subscribable.get())
        expect(5)

        yield()

        expect(7)
        subscribable.set(42)
        assertEquals(42, property.value)
        expect(8)

        yield()

        finish(10)
    }
}
