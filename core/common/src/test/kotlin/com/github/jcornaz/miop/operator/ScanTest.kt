package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.scan
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toList
import org.amshove.kluent.shouldEqual
import kotlin.test.Test

class ScanTest : OperatorTest() {

    override fun <T> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        scan { _, it: T -> it }

    @Test
    fun shouldSendSeed() = runTest {
        emptyReceiveChannel<Int>().scan("test") { _, _ -> unreachable { "scan lambda should not be called" } }
            .toList() shouldEqual listOf("test")
    }

    @Test
    fun shouldSendAccumulation() = runTest {
        receiveChannelOf(1, 2, 3, 4).scan { acc, elt -> acc + elt }
            .toList() shouldEqual listOf(1, 3, 6, 10)
    }

    @Test
    fun shouldSendAccumulationWithSeed() = runTest {
        receiveChannelOf("a", "bc", "def", "ghij").scan(10) { acc, elt -> acc + elt.length }
            .toList() shouldEqual listOf(10, 11, 13, 16, 20)
    }
}
