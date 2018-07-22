package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.ManualTimer
import com.github.jcornaz.miop.internal.test.runTest
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.withContext
import kotlinx.coroutines.experimental.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdatersTest : AsyncTest() {

    private lateinit var timer: ManualTimer

    @Before
    fun setupTimer() {
        timer = ManualTimer()
    }

    @After
    fun terminateTimer() = runTest {
        timer.terminate()
    }

    @Test
    fun propertyUpdaterShouldUpdateTheValue() = runTest {
        val source = Channel<String>(Channel.CONFLATED).apply { send("Hello") }
        val property = SimpleStringProperty()

        var expectedValue = "Hello"
        var nextTime = 1

        withContext(JavaFx) {
            property.addListener { _, _, newValue ->
                assertTrue(Platform.isFxApplicationThread())
                assertEquals(expectedValue, newValue)
                timer.advanceTo(nextTime)
            }
        }

        val job = source.launchUpdater(property)

        withTimeout(1, TimeUnit.SECONDS) {
            timer.await(nextTime)

            expectedValue = "world"
            nextTime = 2
            source.send("world")

            timer.await(nextTime)

            job.cancelAndJoin()
        }
    }

    @Test
    fun listUpdaterShouldAddNewElementsToTheList() = runTest {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("Hello")) }
        val observable = FXCollections.observableArrayList<String>()

        withContext(JavaFx) {
            observable.addListener { _: ListChangeListener.Change<out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = source.launchUpdater(observable)

        withContext(JavaFx) { assertEquals(listOf("Hello"), observable) }

        source.send(listOf("Hello", "world"))

        withContext(JavaFx) { assertEquals(listOf("Hello", "world"), observable) }

        job.cancelAndJoin()
    }

    @Test
    fun listUpdaterShouldRemoveElementsFromTheList() = runTest {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("a", "b", "c", "d")) }
        val observable = FXCollections.observableArrayList<String>()

        withContext(JavaFx) {
            observable.addListener { _: ListChangeListener.Change<out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = source.launchUpdater(observable)

        withContext(JavaFx) { assertEquals(listOf("a", "b", "c", "d"), observable) }

        source.send(listOf("b", "c"))

        withContext(JavaFx) { assertEquals(listOf("b", "c"), observable) }

        job.cancelAndJoin()
    }
}
