package com.github.jcornaz.miop.javafx

import com.github.jcornaz.miop.test.ManualTimer
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("DEPRECATION")
class AdapterTest {

    private lateinit var timer: ManualTimer

    @Before
    fun setupTimer() {
        timer = ManualTimer()
    }

    @After
    fun terminateTimer() = runBlocking {
        timer.terminate()
    }

    @Test
    fun `ObservableValue#asSubscribableValue should return an adapter reflecting the observable`() = runBlocking {
        val property = SimpleIntegerProperty(1)
        val subscribable = property.asSubscribableValue()

        assertEquals(1, subscribable.get())

        launch(coroutineContext) {
            val sub = subscribable.openSubscription()

            assertEquals(1, sub.receive())
            timer.advanceTo(1)
            assertEquals(2, sub.receive())
        }

        timer.await(1)

        withContext(Dispatchers.JavaFx) { property.value = 2 }
        assertEquals(2, subscribable.get())
    }

    @Test
    fun `Property#asSubscribableVariable should return an adapter reflecting the property`() = runBlocking {
        val property = SimpleIntegerProperty(1)
        val subscribable = property.asSubscribableVariable()

        assertEquals(1, subscribable.get())

        launch(coroutineContext) {
            val sub = subscribable.openSubscription()

            assertEquals(1, sub.receive())
            timer.advanceTo(1)
            assertEquals(2, sub.receive())
            timer.advanceTo(2)
            assertEquals(42, sub.receive())
        }

        timer.await(1)

        withContext(Dispatchers.JavaFx) { property.value = 2 }
        assertEquals(2, subscribable.get())

        timer.await(2)

        subscribable.set(42)
        assertEquals(42, withContext(Dispatchers.JavaFx) { property.value })
    }
}
