package com.github.jcornaz.miop.experimental.operator

import com.github.jcornaz.miop.experimental.distinctReferenceUntilChanged
import com.github.jcornaz.miop.experimental.receiveChannelOf
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class DistincReferenceUntilChanged : OperatorTest() {
    override fun ReceiveChannel<Int>.operator(): ReceiveChannel<Int> =
        distinctReferenceUntilChanged()

    @Test
    fun shouldNotEmitLastItemIfItHasTheSameReference() = runTest {
        val ref1 = "Hello"
        val ref2 = "world"

        assertEquals(listOf(ref1, ref2, ref1, ref2), receiveChannelOf(ref1, ref2, ref2, ref2, ref1, ref2).distinctReferenceUntilChanged().toList())
    }
}
