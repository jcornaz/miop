package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.distinctUntilChanged
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class DistinctUtilChangedTest : DistinctReferenceUntilChangedTest() {

    override fun <T : Any> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        distinctUntilChanged()

    @Test
    fun shouldNotEmitLastItemIfEquals() = runTest {
        assertEquals(listOf("a", "b", "a", "b"), receiveChannelOf("a", "a", "b", "b", "a", "b").identityOperation().toList())
    }

    @Test
    fun shouldNotSendTwiceTheSameValueInARow() = runTest {
        val receivedValues = receiveChannelOf(1, 2, 2, 2, 3, 2, 1, 1).distinctUntilChanged().toList()

        assertEquals(listOf(1, 2, 3, 2, 1), receivedValues)
    }
}
