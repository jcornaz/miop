package com.github.jcornaz.miop.operator


import com.github.jcornaz.miop.parallelFilter
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.measureTimeMillis
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParallelFilterTest : OperatorTest() {
    override fun <T> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        parallelFilter { true }

    @Test
    fun shouldParallelize() = runTest {
        withTimeout(1100) {
            receiveChannelOf(1, 2, 3, 4)
                .parallelFilter(concurrency = 4) {
                    delay(1000)
                    true
                }
        }
            .toSet()
    }

    @Test
    fun shouldNoExceedGivenConcurrency() = runTest {
        val result = measureTimeMillis {
            receiveChannelOf(1, 2, 3, 4, 5, 6)
                .parallelFilter(concurrency = 2) {
                    delay(500)
                    true
                }
                .toSet()
        }

        assertTrue(result >= 1500)
    }

    @Test
    fun shouldFilterBasedOnPredicate() = runTest {
        val result = receiveChannelOf(1, 2, 3, 4, 5, 6)
            .parallelFilter(concurrency = 3) { it % 2 == 0 }
            .toSet()

        assertEquals(setOf(2, 4, 6), result)
    }
}
