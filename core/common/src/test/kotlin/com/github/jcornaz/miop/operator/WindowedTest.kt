package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.produce
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.runTest
import com.github.jcornaz.miop.windowed
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowedTest : OperatorTest() {

    override fun <T : Any> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> = windowed(1, 1).map { it.first() }

    @Test
    fun testWindowed() = runTest {

        // given
        val source = receiveChannelOf(1, 2, 3, 4, 5, 6)

        // when
        val windows = source.windowed(3, 2)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = listOf(
                listOf(1, 2, 3),
                listOf(3, 4, 5)
            )
        )
    }

    @Test
    fun testWindowedWithStepBiggerThanSize() = runTest {

        // given
        val source = receiveChannelOf(1, 2, 3, 4, 5, 6, 7, 8, 9)

        // when
        val windows = source.windowed(3, 4, true)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = listOf(
                listOf(1, 2, 3),
                listOf(5, 6, 7),
                listOf(9)
            )
        )
    }

    @Test
    fun testPartialWindow() = runTest {

        // given
        val source = receiveChannelOf(1, 2, 3, 4, 5, 6)

        // when
        val windows = source.windowed(3, 2, true)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = listOf(
                listOf(1, 2, 3),
                listOf(3, 4, 5),
                listOf(5, 6)
            )
        )
    }

    @Test
    fun windowedWithStepBiggerThanSizeShouldBeConsistentWithSequenceWindow() = runTest {

        // given
        val sequence = generateSequence(0) { it + 1 }.take(42)
        val source = produce(sequence)

        // when
        val windows = source.windowed(3, 4)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = sequence.windowed(3, 4).toList()
        )
    }

    @Test
    fun windowShouldBeConsistentWithSequenceWindow() = runTest {

        // given
        val sequence = generateSequence(0) { it + 1 }.take(42)
        val source = produce(sequence)

        // when
        val windows = source.windowed(4, 2)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = sequence.windowed(4, 2).toList()
        )
    }

    @Test
    fun windowWithPartialShouldBeConsistentWithSequenceWindowWithPartial() = runTest {

        // given
        val sequence = generateSequence(0) { it + 1 }.take(42)
        val source = produce(sequence)

        // when
        val windows = source.windowed(4, 2, true)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = sequence.windowed(4, 2, true).toList()
        )
    }

    @Test
    fun windowDefaultShouldBeConsistentWithSequenceDefault() = runTest {

        // given
        val sequence = generateSequence(0) { it + 1 }.take(42)
        val source = produce(sequence)

        // when
        val windows = source.windowed(4)

        // then
        assertEquals(
            actual = windows.toList(),
            expected = sequence.windowed(4).toList()
        )
    }
}
