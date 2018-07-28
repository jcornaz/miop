package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.ManualTimer
import com.github.jcornaz.miop.internal.test.runTest
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.SetChangeListener
import kotlinx.coroutines.experimental.Job
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

        val job = source.launchFxUpdater(property)

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

        val job = source.launchFxListUpdater(observable)

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

        val job = source.launchFxListUpdater(observable)

        withContext(JavaFx) { assertEquals(listOf("a", "b", "c", "d"), observable) }

        source.send(listOf("b", "c"))

        withContext(JavaFx) { assertEquals(listOf("b", "c"), observable) }

        job.cancelAndJoin()
    }

    @Test
    fun collectionUpdaterShouldAddNewElementsToTheList() = runTest {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("Hello")) }
        val observable = FXCollections.observableArrayList<String>()

        withContext(JavaFx) {
            observable.addListener { _: ListChangeListener.Change<out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = source.launchFxCollectionUpdater(observable)

        withContext(JavaFx) { assertEquals(listOf("Hello"), observable) }

        source.send(listOf("Hello", "world"))

        withContext(JavaFx) { assertEquals(listOf("Hello", "world"), observable) }

        job.cancelAndJoin()
    }

    @Test
    fun collectionUpdaterShouldRemoveElementsFromTheList() = runTest {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("a", "b", "c", "d")) }
        val observable = FXCollections.observableArrayList<String>()

        withContext(JavaFx) {
            observable.addListener { _: ListChangeListener.Change<out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = source.launchFxCollectionUpdater(observable)

        withContext(JavaFx) { assertEquals(listOf("a", "b", "c", "d"), observable) }

        source.send(listOf("b", "c"))

        withContext(JavaFx) { assertEquals(listOf("b", "c"), observable) }

        job.cancelAndJoin()
    }

    @Test
    fun collectionUpdaterShouldBeAbleToUpdateSetFromList() = runTest {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("a", "b", "b", "a")) }
        val observable = FXCollections.observableSet(HashSet<String>())

        withContext(JavaFx) {
            observable.addListener { _: SetChangeListener.Change<out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = source.launchFxCollectionUpdater(observable)

        withContext(JavaFx) { assertEquals(setOf("a", "b"), observable) }

        source.send(listOf("b", "c"))

        withContext(JavaFx) { assertEquals(setOf("b", "c"), observable) }

        job.cancelAndJoin()
    }

    @Test
    fun collectionUpdaterShouldNotConsiderOrder() = runTest {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("a", "b", "c", "d", "c")) }
        val observable = FXCollections.observableArrayList<String>("c", "b", "x", "a")

        withContext(JavaFx) {
            observable.addListener { _: ListChangeListener.Change<out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = Job()

        withContext(JavaFx) {
            source.launchFxCollectionUpdater(observable, job)
            assertEquals(listOf("c", "b", "a", "d", "c"), observable)
        }

        source.send(listOf("a", "b"))

        withContext(JavaFx) { assertEquals(listOf("b", "a"), observable) }

        job.cancelAndJoin()
    }

    @Test
    fun mapUpdaterShouldAddNewEntries() = runTest {
        val source = Channel<Map<Int, String>>(Channel.CONFLATED).apply { send(mapOf(0 to "Hello")) }
        val observable = FXCollections.observableHashMap<Int, String>()

        withContext(JavaFx) {
            observable.addListener { _: MapChangeListener.Change<out Int, out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = source.launchFxMapUpdater(observable)

        withContext(JavaFx) { assertEquals(mapOf(0 to "Hello"), observable) }

        source.send(mapOf(0 to "Hello", 1 to "world"))

        withContext(JavaFx) { assertEquals(mapOf(0 to "Hello", 1 to "world"), observable) }

        job.cancelAndJoin()
    }

    @Test
    fun mapUpdaterShouldUpdatesEntries() = runTest {
        val source = Channel<Map<Int, String>>(Channel.CONFLATED).apply { send(mapOf(0 to "Hello", 1 to "world")) }
        val observable = FXCollections.observableHashMap<Int, String>()

        withContext(JavaFx) {
            observable.addListener { _: MapChangeListener.Change<out Int, out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = source.launchFxMapUpdater(observable)

        withContext(JavaFx) { assertEquals(mapOf(0 to "Hello", 1 to "world"), observable) }

        source.send(mapOf(0 to "Hello", 1 to "kotlin"))

        withContext(JavaFx) { assertEquals(mapOf(0 to "Hello", 1 to "kotlin"), observable) }

        job.cancelAndJoin()
    }

    @Test
    fun mapUpdaterShouldRemoveEntries() = runTest {
        val source = Channel<Map<Int, String>>(Channel.CONFLATED).apply { send(mapOf(0 to "Hello", 1 to "world")) }
        val observable = FXCollections.observableHashMap<Int, String>()

        withContext(JavaFx) {
            observable.addListener { _: MapChangeListener.Change<out Int, out String> ->
                assertTrue(Platform.isFxApplicationThread())
            }
        }

        val job = source.launchFxMapUpdater(observable)

        withContext(JavaFx) { assertEquals(mapOf(0 to "Hello", 1 to "world"), observable) }

        source.send(mapOf(0 to "Hello"))

        withContext(JavaFx) { assertEquals(mapOf(0 to "Hello"), observable) }

        job.cancelAndJoin()
    }
}
