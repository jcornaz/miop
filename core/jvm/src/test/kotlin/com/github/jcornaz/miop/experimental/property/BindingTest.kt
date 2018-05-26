package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.AsyncTest
import kotlinx.coroutines.experimental.*
import org.junit.Test
import kotlin.test.assertEquals

class BindingTest : AsyncTest() {

    @Test
    fun `bind should keep the target variable up-to-date when the source change`() = runBlocking {
        val source = SubscribableVariable(0)
        val target = SubscribableVariable(0)

        val job = target.bind(source)

        expect(1)
        launch(coroutineContext, CoroutineStart.UNDISPATCHED) {
            expect(2)
            val sub = target.openSubscription()
            assertEquals(0, sub.receive())
            expect(3)
            assertEquals(1, sub.receive())
            expect(6)
            assertEquals(2, sub.receive())
            expect(11)
        }

        expect(4)
        assertEquals(0, target.get())
        source.set(1)
        assertEquals(1, target.get())
        expect(5)
        yield()
        expect(7)
        job.cancel() // should stop the binding
        source.set(2)
        assertEquals(1, target.get())
        expect(8)
        yield()
        expect(9)
        target.bind(source) // rebind
        assertEquals(2, target.get())
        expect(10)
        yield()
        finish(12)
    }

    @Test
    fun `it should be possible to cancel a binding with a parent job`() = runBlocking {
        val source = SubscribableVariable(0)
        val target = SubscribableVariable(0)

        val job = Job()

        target.bind(source, job)

        assertEquals(0, target.get())
        source.set(1)
        assertEquals(1, target.get())
        job.cancel() // should stop the binding
        source.set(2)
        assertEquals(1, target.get())
    }

    @Test(timeout = 1000)
    fun `bindBidirectional should keep up-to-date both variable`() = runBlocking {
        val variable1 = SubscribableVariable(0)
        val variable2 = SubscribableVariable(0)

        val job = variable2.bindBidirectional(variable1)

        assertEquals(0, variable1.get())
        assertEquals(0, variable1.get())

        variable1.set(1)
        assertEquals(1, variable1.get())
        assertEquals(1, variable2.get())
        variable2.set(2)
        assertEquals(2, variable1.get())
        assertEquals(2, variable2.get())

        job.cancel()

        variable1.set(3)
        assertEquals(3, variable1.get())
        assertEquals(2, variable2.get())
    }
}
