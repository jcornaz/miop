package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.property.StateStore
import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.ManualTimer
import com.github.jcornaz.miop.internal.test.runTest
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BindingTest : AsyncTest() {

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
    fun propertyBindingShouldUpdateTheProperty() = runTest {
        val store = StateStore(0)
        val property = SimpleIntegerProperty(0)

        val job = property.bind(store)

        property.addListener { _, oldValue, newValue ->
            assertTrue(Platform.isFxApplicationThread())
            assertEquals(0, oldValue)
            assertEquals(42, newValue)
            timer.advanceTo(1)
        }

        store.dispatch { 42 }
        withTimeout(1, TimeUnit.SECONDS) {
            timer.await(1)
        }

        job.cancelAndJoin()
    }

    @Test
    fun listBindingShouldUpdateTheList() = runTest {
        val store = StateStore(listOf(1, 2, 3))
        val list = mutableListOf<Int>()

        list
    }
}