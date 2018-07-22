package com.github.jcornaz.miop.javafx.experimental

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

class UpdatersTest {

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
    fun listUpdaterShouldUpdateTheList() = runTest {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("Hello")) }
        val observable = FXCollections.observableArrayList<String>()

        var expectedValue = listOf("Hello")
        var nextTime = 1

        withContext(JavaFx) {
            observable.addListener { change: ListChangeListener.Change<out String> ->
                assertTrue(change.next())
                assertTrue(change.wasAdded())
                assertEquals(expectedValue, change.list)
                timer.advanceTo(nextTime)
            }
        }

        val job = source.launchUpdater(observable)

        withTimeout(1, TimeUnit.SECONDS) {
            timer.await(nextTime)

            expectedValue = listOf("Hello", "world")
            nextTime = 2
            source.send(expectedValue)

            timer.await(nextTime)

            job.cancelAndJoin()
        }
    }
}
