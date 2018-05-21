package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.internal.test.AsyncTest
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class AdapterTest : AsyncTest() {

    @Test
    fun `subscribable adapter should reflect the subscribable variable`() = runBlocking {
        val property = SimpleIntegerProperty(1)
        val subscribable = property.asSubscribableValue()

        assertEquals(1, subscribable.get())

        property.value = 2
        assertEquals(2, subscribable.get())

        property.value = 42
        assertEquals(42, subscribable.get())
    }

}
