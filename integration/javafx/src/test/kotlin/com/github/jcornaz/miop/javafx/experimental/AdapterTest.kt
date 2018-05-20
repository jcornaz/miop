package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.property.SubscribableVariable
import com.github.jcornaz.miop.internal.test.AsyncTest
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class AdapterTest : AsyncTest() {

    @Test
    fun `subscribable adapter adapter should reflect the subscribable value`() {
        val subscribable = SubscribableVariable(1)
        val observable = subscribable.asObservableValue()

        assertEquals(1, observable.value)

        subscribable.value = 2
        assertEquals(2, observable.value)
    }

    @Test
    fun `subscribable variable adapter should reflect the subscribable variable`() {
        val subscribable = SubscribableVariable<Int?>(1)
        val property = subscribable.asProperty()

        assertEquals(1, property.value)

        subscribable.value = 2
        assertEquals(2, property.value)

        property.value = 42
        assertEquals(42, subscribable.value)
    }

    @Test
    fun `observable value adapter should reflect the subscribable variable`() = runBlocking {
        val property = SimpleIntegerProperty(1)
        val subscribable = property.asSubscribableValue()

        assertEquals(1, subscribable.value)

        property.value = 2
        assertEquals(2, subscribable.value)

        property.value = 42
        assertEquals(42, subscribable.value)
    }

    @Test
    fun `property adapter should reflect the subscribable value`() {
        val subscribable = SubscribableVariable<Int?>(1)
        val property = subscribable.asProperty()

        assertEquals(1, property.value)

        subscribable.value = 2
        assertEquals(2, property.value)
    }
}
