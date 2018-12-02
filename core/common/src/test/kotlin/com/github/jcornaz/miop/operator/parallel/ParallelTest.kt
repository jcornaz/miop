package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.parallel
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.toSet
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelTest : ParallelOperatorTest() {
    override fun <T> ReceiveChannel<T>.identityDelayedOperation(parallelism: Int, delayTime: Long): ReceiveChannel<T> =
        parallel(parallelism) {
            map {
                delay(delayTime)
                it
            }
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
