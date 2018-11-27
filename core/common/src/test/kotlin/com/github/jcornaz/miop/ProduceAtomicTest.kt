package com.github.jcornaz.miop

import com.github.jcornaz.miop.operator.DummyException
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProduceAtomicTest {

    @Test
    fun shouldReturnEmptyChannel() = runTest {
        assertTrue(produceAtomic<Int> { }.toList().isEmpty())
    }

    @Test
    fun shouldReturnSentElements() = runTest {
        val result = produceAtomic<Int> {
            repeat(5) {
                delay(100)
                send(it)
            }
        }

        assertEquals(listOf(0, 1, 2, 3, 4), result.toList())
    }

    @Test
    fun shouldTransmitErrorToDownstream() = runTest {
        val result = GlobalScope.produceAtomic<Int> {
            throw DummyException("my exception")
        }

        val exception = assertThrows<DummyException> { result.receive() }
        assertEquals("my exception", exception.message)
    }

    @Test
    fun shouldTransmitErrorToParentScope() = runTest {
        val exception = assertThrows<DummyException> {
            coroutineScope {
                produceAtomic<Int> {
                    throw DummyException("my exception")
                }
            }
        }
        assertEquals("my exception", exception.message)
    }

    @Test
    fun shouldBeChildOfTheParentScope() = runTest {
        var done = false
        coroutineScope {
            produceAtomic<Int> {
                delay(1000)
                done = true
            }
        }
        assertTrue(done)
    }

    @Test
    fun shouldInvokeBlockEvenWhenCancelled() = runTest {
        repeat(100) {
            val completion = CompletableDeferred<Unit>()

            produceAtomic<Int>(Dispatchers.Default) {
                completion.complete(Unit)
            }.cancel()

            withTimeout(500) {
                completion.await()
            }
        }
    }
}