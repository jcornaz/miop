package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.parallelMap
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toSet
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelMapTest : ParallelOperatorTest() {
    override fun <T> ReceiveChannel<T>.identityDelayedOperation(parallelism: Int, delayTime: Long): ReceiveChannel<T> =
        parallelMap(parallelism= parallelism) {
            delay(delayTime)
            it
        }

    @Test
    fun shouldApplyTransform() = runTest {
        val result = receiveChannelOf(1, 2, 3)
            .parallelMap(parallelism = 3) { (it * 2).toString() }
            .toSet()

        assertEquals(setOf("2", "4", "6"), result)
    }
}
