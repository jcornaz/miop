package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.parallel
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.measureTimeMillis
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.toSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParallelTest : OperatorTest() {
    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> =
        parallel { map { it } }

    @Test
    fun shouldParallelize() = runTest {
        withTimeout(1100) {
            receiveChannelOf(1, 2, 3, 4)
                .parallel(4) {
                    map {
                        delay(1000)
                        it
                    }
                }
                .toSet()
        }
    }

    @Test
    fun shouldNoExceedGivenConcurrency() = runTest {
        val result = measureTimeMillis {
            receiveChannelOf(1, 2, 3, 4, 5, 6)
                .parallel(2) {
                    map {
                        delay(500)
                        it
                    }
                }
                .toSet()
        }

        assertTrue(result >= 1500)
    }

    @Test
    fun shouldApplyInnerOperators() = runTest {
        val result = receiveChannelOf(1, 2, 3, 4, 5, 6)
            .parallel(3) {
                filter { it % 2 == 0 }
                    .map { it + 1 }
                    .map { it.toString() }
            }
            .toSet()

        assertEquals(setOf("3", "5", "7"), result)
    }
}
