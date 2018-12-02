package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.parallelFilter
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toSet
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelFilterTest : ParallelOperatorTest() {
    
    override fun <T> ReceiveChannel<T>.identityDelayedOperation(parallelism: Int, delayTime: Long): ReceiveChannel<T> =
        parallelFilter(parallelism = parallelism) {
            delay(delayTime)
            true
        }

    @Test
    fun shouldFilterBasedOnPredicate() = runTest {
        val result = receiveChannelOf(1, 2, 3, 4, 5, 6)
            .parallelFilter(parallelism = 3) { it % 2 == 0 }
            .toSet()

        assertEquals(setOf(2, 4, 6), result)
    }
}
