package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.internal.test.ManualTimer
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JavaFxUtilsTest {

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
        val context = coroutineContext + JavaFx

        val observable = mock<ObservableValue<Int>> {
            on { value } doReturn 42
            on { addListener(any<ChangeListener<in Int>>()) }.thenAnswer {
                val listener = it.getArgument(0) as ChangeListener<in Int>

                launch(context) {
                    timer.await(1)
                    listener.changed(it.mock as ObservableValue<out Int>, 42, 1)

                    timer.await(2)
                    listener.changed(it.mock as ObservableValue<out Int>, 1, 2)
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
}
