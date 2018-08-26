package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.distinctUntilChanged
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class DistinctUtilChangedTest : DistinctReferenceUntilChangedTest() {

    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> =
        distinctUntilChanged()

    @Test
    fun shouldNotEmitLastItemIfEquals() = runTest {
        assertEquals(listOf("a", "b", "a", "b"), receiveChannelOf("a", "a", "b", "b", "a", "b").operator().toList())
    }
}