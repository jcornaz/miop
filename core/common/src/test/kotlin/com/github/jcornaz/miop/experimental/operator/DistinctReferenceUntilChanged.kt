package com.github.jcornaz.miop.experimental.operator

import com.github.jcornaz.miop.experimental.distinctReferenceUntilChanged
import com.github.jcornaz.miop.experimental.receiveChannelOf
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

open class DistinctReferenceUntilChanged : OperatorTest() {

    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> =
        distinctReferenceUntilChanged()

    @Test
    fun shouldNotEmitLastItemIfItHasTheSameReference() = runTest {
        val ref1 = "Hello"
        val ref2 = "world"

        assertEquals(listOf(ref1, ref2, ref1, ref2), receiveChannelOf(ref1, ref2, ref2, ref2, ref1, ref2).operator().toList())
    }
}