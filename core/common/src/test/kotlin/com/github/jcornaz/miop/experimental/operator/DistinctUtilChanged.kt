package com.github.jcornaz.miop.experimental.operator

import com.github.jcornaz.miop.experimental.distinctUntilChanged
import com.github.jcornaz.miop.experimental.receiveChannelOf
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class DistinctUtilChanged : DistinctReferenceUntilChanged() {

    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> =
        distinctUntilChanged()

    @Test
    fun shouldNotEmitLastItemIfEquals() = runTest {
        assertEquals(listOf("a", "b", "a", "b"), receiveChannelOf("a", "a", "b", "b", "a", "b").operator().toList())
    }
}