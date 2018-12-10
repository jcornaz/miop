package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.filterIsInstance
import com.github.jcornaz.miop.produce
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterIsInstanceTest : OperatorTest() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        filterIsInstance<Any?>().map { it as T }

    @Test
    fun testFilterIsInstance() = runTest {
        val elements = listOf("a", "b", 1, 2, "c", 3, "d")

        assertEquals(listOf("a", "b", "c", "d"), produce(elements).filterIsInstance<String>().toList())
        assertEquals(listOf(1, 2, 3), produce(elements).filterIsInstance<Int>().toList())
    }
}
