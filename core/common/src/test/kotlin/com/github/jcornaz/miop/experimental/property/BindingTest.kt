package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.ManualTimer
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BindingTest : AsyncTest() {

    private lateinit var timer: ManualTimer

    @BeforeTest
    fun setupTimer() {
        timer = ManualTimer()
    }

    @AfterTest
    fun terminateTimer() = runTest {
        timer.terminate()
    }

    @Test
    fun bindShouldKeepTheTargetVariableUpToDateWhenTheSourceChange() = runTest {
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
    fun itShouldBePossibleToCancelABindingWithAParentJob() = runTest {
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

    @Test
    fun storeBindingShouldDispatchActions() = runTest {
        val variable = SubscribableVariable(0)
        val store = StateStore(0)

        val job = store.bind(variable) { { it } }

        val timer = ManualTimer()

        launch(coroutineContext, CoroutineStart.UNDISPATCHED) {
            val sub = store.openSubscription()
            assertEquals(0, sub.receive())
            timer.advanceTo(1)
            assertEquals(42, sub.receive())
        }

        timer.await(0)
        variable.set(1)
        timer.await(1)
        variable.set(42)
        job.join()
    }
}
