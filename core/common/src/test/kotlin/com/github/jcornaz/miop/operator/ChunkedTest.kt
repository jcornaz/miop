package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.chunked
import com.github.jcornaz.miop.openSubscription
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class ChunkedTest : OperatorTest() {

    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> = chunked(1).map { it.first() }

    @Test
    fun testChunked() = runTest {
        // given
        val source = receiveChannelOf(1, 2, 3, 4, 5, 6, 7)

        // when
        val windows = source.chunked(3)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6),
                listOf(7)
            )
        )
    }

    @Test
    fun chunkedShouldBeConsistentWithSequenceChunked() = runTest {
        // given
        val sequence = generateSequence(0) { it + 1 }.take(42)
        val source = sequence.openSubscription()

        // when
        val windows = source.chunked(7)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = sequence.chunked(7).toList()
        )
    }
}
