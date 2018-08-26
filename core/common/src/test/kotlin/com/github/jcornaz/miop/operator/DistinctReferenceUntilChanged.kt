package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.distinctReferenceUntilChanged
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toList
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
