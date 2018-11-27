package com.github.jcornaz.miop.javafx

import com.github.jcornaz.collekt.api.PersistentList
import com.github.jcornaz.miop.test.ManualTimer
import com.github.jcornaz.miop.test.runTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubscriptionsTest {

    private lateinit var timer: ManualTimer

    @Before
    fun setupTimer() {
        timer = ManualTimer()
    }

    @After
    fun terminateTimer() = runBlocking {
        timer.terminate()
    }

    @Test(timeout = 10_000)
    @Suppress("UNCHECKED_CAST")
    fun `ObservableValue#openSubscription should add listener to the observable value`() = runBlocking {
        val context = coroutineContext + Dispatchers.JavaFx

        val observable = mock<ObservableValue<Int>> {
            on { value } doReturn 42
            on { addListener(any<ChangeListener<in Int>>()) }.thenAnswer { invocation ->
                val listener = invocation.getArgument(0) as ChangeListener<in Int>

                launch(context) {
                    timer.await(1)
                    listener.changed(invocation.mock as ObservableValue<out Int>, 42, 1)

                    timer.await(2)
                    listener.changed(invocation.mock as ObservableValue<out Int>, 1, 2)
                }
            }
        }

        val subscription = observable.openValueSubscription()

        assertEquals(42, subscription.receive())
        timer.advanceTo(1)

        assertEquals(1, subscription.receive())
        timer.advanceTo(2)

        assertEquals(2, subscription.receive())
    }


    @Test // (timeout = 10_000)
    fun `cancelling the subscription returned by ObservableValue#openSubscription should remove the listener from the observable value`() = runBlocking {
        val observable = mock<ObservableValue<Int>> {
            on { value } doReturn 0
            on { addListener(any<ChangeListener<in Int>>()) } doAnswer { timer.advanceTo(1) }
            on { removeListener(any<ChangeListener<in Int>>()) } doAnswer { timer.advanceTo(2) }
        }

        val sub = observable.openValueSubscription()

        timer.await(1)
        sub.cancel()
        timer.await(2)
    }

    @Test
    fun `ObservableValue#openSubscription should access the observable from the JavaFx thread`() = runBlocking {
        val observable = mock<ObservableValue<Int>> {
            on { value } doAnswer { assertTrue(Platform.isFxApplicationThread()); 0 }
            on { addListener(any<ChangeListener<in Int>>()) } doAnswer { assertTrue(Platform.isFxApplicationThread()); timer.advanceTo(1) }
            on { removeListener(any<ChangeListener<in Int>>()) } doAnswer { assertTrue(Platform.isFxApplicationThread()); timer.advanceTo(2) }
        }

        val sub = observable.openValueSubscription()

        timer.await(1)
        sub.cancel()
        timer.await(2)
    }

    @Test
    fun testObservableListSubscription() = runTest {
        val observable = FXCollections.observableArrayList<String>("Hello", "world")
        val sub: ReceiveChannel<PersistentList<String>> = observable.openListSubscription()

        launch(coroutineContext) {
            assertEquals(listOf("Hello", "world"), sub.receive())
            timer.advanceTo(1)
            timer.await(2)
            assertEquals(listOf("Hello", "Kotlin"), sub.receive())
            timer.advanceTo(3)
            timer.await(4)
            assertEquals(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"), sub.receive())
            timer.advanceTo(5)
        }

        timer.await(1)

        withContext(Dispatchers.JavaFx) { observable[1] = "Kotlin" }
        timer.advanceTo(2)
        timer.await(3)

        withContext(Dispatchers.JavaFx) {
            observable.clear()
            observable.addAll("one", "two")
            observable.remove("one")
            observable[0] = "Monday"
        }

        withContext(Dispatchers.JavaFx) {
            observable.addAll("Tuesday", "Wednesday", "Thursday", "Friday")
        }

        timer.advanceTo(4)
        timer.await(5)
    }

    @Test
    fun testObservableListSubscriptionWithSorting() = runTest {
        val observable = FXCollections.observableArrayList<Char>('a', 'x', 'd', 'b', 'c', 'y', 'z')
        val sub: ReceiveChannel<PersistentList<Char>> = observable.openListSubscription()

        launch(coroutineContext) {
            assertEquals(listOf('a', 'x', 'd', 'b', 'c', 'y', 'z'), sub.receive())
            timer.advanceTo(1)
            timer.await(2)
            assertEquals(listOf('a', 'b', 'c', 'd', 'x', 'y', 'z'), sub.receive())
        }

        timer.await(1)
        withContext(Dispatchers.JavaFx) { observable.sort() }
        timer.advanceTo(2)
    }

    @Test
    fun testObservableListSubscriptionWithInsert() = runTest {
        val observable = FXCollections.observableArrayList<Char>('a', 'd', 'e')
        val sub: ReceiveChannel<PersistentList<Char>> = observable.openListSubscription()

        launch(coroutineContext) {
            assertEquals(listOf('a', 'd', 'e'), sub.receive())
            timer.advanceTo(1)
            timer.await(2)
            assertEquals(listOf('a', 'b', 'c', 'd', 'e'), sub.receive())
        }

        timer.await(1)
        withContext(Dispatchers.JavaFx) {
            observable.add(1, 'c')
            observable.add(1, 'b')
        }
        timer.advanceTo(2)
    }
}
