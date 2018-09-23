package com.github.jcornaz.miop.javafx

import com.github.jcornaz.miop.test.AsyncTest
import com.github.jcornaz.miop.test.ManualTimer
import com.github.jcornaz.miop.test.runTest
import com.github.jcornaz.miop.test.delayTest
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.SetChangeListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.javafx.JavaFx
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertSame
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
    fun propertyUpdaterShouldUpdateTheValue() = runTest(Dispatchers.JavaFx) {
        val source = Channel<String>(Channel.CONFLATED).apply { send("Hello") }
        val property = SimpleStringProperty()

        var expectedValue = "Hello"
        var nextTime = 1

        property.addListener { _, _, newValue ->
            assertTrue(Platform.isFxApplicationThread())
            assertEquals(expectedValue, newValue)
            timer.advanceTo(nextTime)
        }

        val job = launchFxUpdater(property, source)

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
    fun listUpdaterShouldAddNewElementsToTheList() = runTest(Dispatchers.JavaFx) {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("Hello")) }
        val observable = FXCollections.observableArrayList<String>()

        observable.addListener { _: ListChangeListener.Change<out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = launchFxListUpdater(observable, source)

        assertEquals(listOf("Hello"), observable)

        source.send(listOf("Hello", "world"))

        delayTest()

        assertEquals(listOf("Hello", "world"), observable)

        job.cancelAndJoin()
    }

    @Test
    fun listUpdaterShouldRemoveElementsFromTheList() = runTest(Dispatchers.JavaFx) {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("a", "b", "c", "d")) }
        val observable = FXCollections.observableArrayList<String>()

        observable.addListener { _: ListChangeListener.Change<out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = launchFxListUpdater(observable, source)

        assertEquals(listOf("a", "b", "c", "d"), observable)

        source.send(listOf("b", "c"))

        delayTest()

        assertEquals(listOf("b", "c"), observable)

        job.cancelAndJoin()
    }

    @Test
    fun collectionUpdaterShouldAddNewElementsToTheList() = runTest(Dispatchers.JavaFx) {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("Hello")) }
        val observable = FXCollections.observableArrayList<String>()

        observable.addListener { _: ListChangeListener.Change<out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = launchFxCollectionUpdater(observable, source)

        assertEquals(listOf("Hello"), observable)

        source.send(listOf("Hello", "world"))

        delayTest()

        assertEquals(listOf("Hello", "world"), observable)

        job.cancelAndJoin()
    }

    @Test
    fun collectionUpdaterShouldRemoveElementsFromTheList() = runTest(Dispatchers.JavaFx) {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("a", "b", "c", "d")) }
        val observable = FXCollections.observableArrayList<String>()

        observable.addListener { _: ListChangeListener.Change<out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = launchFxCollectionUpdater(observable, source)

        assertEquals(listOf("a", "b", "c", "d"), observable)

        source.send(listOf("b", "c"))

        delayTest()

        assertEquals(listOf("b", "c"), observable)

        job.cancelAndJoin()
    }

    @Test
    fun collectionUpdaterShouldBeAbleToUpdateSetFromList() = runTest(Dispatchers.JavaFx) {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("a", "b", "b", "a")) }
        val observable = FXCollections.observableSet(HashSet<String>())

        observable.addListener { _: SetChangeListener.Change<out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = launchFxCollectionUpdater(observable, source)

        assertEquals(setOf("a", "b"), observable)

        source.send(listOf("b", "c"))

        delayTest()

        assertEquals(setOf("b", "c"), observable)

        job.cancelAndJoin()
    }

    @Test
    @Suppress("DEPRECATION")
    fun collectionUpdaterShouldNotConsiderOrder() = runTest(Dispatchers.JavaFx) {
        val source = Channel<List<String>>(Channel.CONFLATED).apply { send(listOf("a", "b", "c", "d", "c")) }
        val observable = FXCollections.observableArrayList<String>("c", "b", "x", "a")

        observable.addListener { _: ListChangeListener.Change<out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = Job()

        source.launchFxCollectionUpdater(observable, job)
        assertEquals(listOf("c", "b", "a", "d", "c"), observable)

        source.send(listOf("a", "b"))

        delayTest()

        assertEquals(listOf("b", "a"), observable)

        job.cancelAndJoin()
    }

    @Test
    fun mapUpdaterShouldAddNewEntries() = runTest(Dispatchers.JavaFx) {
        val source = Channel<Map<Int, String>>(Channel.CONFLATED).apply { send(mapOf(0 to "Hello")) }
        val observable = FXCollections.observableHashMap<Int, String>()

        observable.addListener { _: MapChangeListener.Change<out Int, out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = launchFxMapUpdater(observable, source)

        assertEquals(mapOf(0 to "Hello"), observable)

        source.send(mapOf(0 to "Hello", 1 to "world"))

        delayTest()

        assertEquals(mapOf(0 to "Hello", 1 to "world"), observable)

        job.cancelAndJoin()
    }

    @Test
    fun mapUpdaterShouldUpdatesEntries() = runTest(Dispatchers.JavaFx) {
        val source = Channel<Map<Int, String>>(Channel.CONFLATED).apply { send(mapOf(0 to "Hello", 1 to "world")) }
        val observable = FXCollections.observableHashMap<Int, String>()

        observable.addListener { _: MapChangeListener.Change<out Int, out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = launchFxMapUpdater(observable, source)

        assertEquals(mapOf(0 to "Hello", 1 to "world"), observable)

        source.send(mapOf(0 to "Hello", 1 to "kotlin"))

        delayTest()

        assertEquals(mapOf(0 to "Hello", 1 to "kotlin"), observable)

        job.cancelAndJoin()
    }

    @Test
    fun mapUpdaterShouldRemoveEntries() = runTest(Dispatchers.JavaFx) {
        val source = Channel<Map<Int, String>>(Channel.CONFLATED).apply { send(mapOf(0 to "Hello", 1 to "world")) }
        val observable = FXCollections.observableHashMap<Int, String>()

        observable.addListener { _: MapChangeListener.Change<out Int, out String> ->
            assertTrue(Platform.isFxApplicationThread())
        }

        val job = launchFxMapUpdater(observable, source)

        assertEquals(mapOf(0 to "Hello", 1 to "world"), observable)

        source.send(mapOf(0 to "Hello"))

        delayTest()

        assertEquals(mapOf(0 to "Hello"), observable)

        job.cancelAndJoin()
    }

    @Test
    fun updateListFromKeySetShouldAddItems() = runTest(Dispatchers.JavaFx) {
        val source = Channel<Set<Int>>(Channel.CONFLATED).apply { send(setOf(0, 1, 2, 3)) }
        val observableList = FXCollections.observableArrayList<Item>()

        val job = launchFxListUpdater(observableList, source, Item::dispose, ::Item)

        assertEquals(listOf(0, 1, 2, 3), observableList.map { it.value })
        assertTrue(observableList.none { it.isDisposed })

        source.send(setOf(0, 1, 2, 3, 4))

        delayTest()

        assertEquals(listOf(0, 1, 2, 3, 4), observableList.map { it.value })
        assertTrue(observableList.none { it.isDisposed })

        job.cancelAndJoin()
        assertEquals(listOf(0, 1, 2, 3, 4), observableList.map { it.value })
        assertTrue(observableList.all { it.isDisposed })
    }

    @Test
    fun updateListFromKeySetShouldRemoveItems() = runTest(Dispatchers.JavaFx) {
        val source = Channel<Set<Int>>(Channel.CONFLATED).apply { send(setOf(0, 1, 2, 3)) }
        val observableList = FXCollections.observableArrayList<Item>()

        val job = launchFxListUpdater(observableList, source, Item::dispose, ::Item)

        assertEquals(listOf(0, 1, 2, 3), observableList.map { it.value })
        assertTrue(observableList.none { it.isDisposed })

        val previousItems = observableList.toList()

        source.send(setOf(0, 2))

        delayTest()

        assertEquals(listOf(0, 2), observableList.map { it.value })
        assertTrue(observableList.none { it.isDisposed })
        assertSame(previousItems[0], observableList[0])
        assertTrue(previousItems[1].isDisposed)
        assertSame(previousItems[2], observableList[1])
        assertTrue(previousItems[3].isDisposed)

        job.cancelAndJoin()
        assertEquals(listOf(0, 2), observableList.map { it.value })
        assertTrue(observableList.all { it.isDisposed })
    }

    @Test
    fun updateListFromKeySetShouldClearItems() = runTest(Dispatchers.JavaFx) {
        val source = Channel<Set<Int>>(Channel.CONFLATED).apply { send(setOf(0, 1, 2, 3)) }
        val observableList = FXCollections.observableArrayList<Item>()

        val job = launchFxListUpdater(observableList, source, Item::dispose, ::Item)

        assertEquals(listOf(0, 1, 2, 3), observableList.map { it.value })
        assertTrue(observableList.none { it.isDisposed })

        val previousItems = observableList.toList()

        source.send(emptySet())

        delayTest()

        assertTrue(observableList.isEmpty())
        assertTrue(previousItems.all { it.isDisposed })

        job.cancelAndJoin()
    }

    private class Item(val value: Int) {
        var isDisposed = false
            private set

        fun dispose() {
            check(!isDisposed)
            isDisposed = true
        }
    }
}
