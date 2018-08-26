package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.debounce
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.delay
import org.junit.Test
import kotlin.test.assertEquals

class DebounceTest : OperatorTest() {
    override fun <T> ReceiveChannel<T>.operator(): ReceiveChannel<T> = debounce(0)

    @Test
    fun testDebounce() = runTest {
        val source = produce {
            send(1)
            send(2)
            delay(100)
            send(3)
            delay(600)
            send(4)
            delay(200)
            send(5)
        }

        assertEquals(listOf(3, 5), source.debounce(500).toList())
    }
}
