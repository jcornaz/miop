package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.emptyReceiveChannel
import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.scan
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.toList
import org.amshove.kluent.shouldEqual
import kotlin.test.Test

class ScanWithInitialValue : OperatorTest() {

    override fun <T : Any> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        scan<T, T?>(null) { _, t -> t }.filterNotNull()

    @Test
    fun shouldSendInitialValue() = runTest {
        emptyReceiveChannel<Int>().scan("test") { _, _ -> unreachable { "scan lambda should not be called" } }
            .toList() shouldEqual listOf("test")
    }

    @Test
    fun shouldSendAccumulation() = runTest {
        receiveChannelOf("a", "bc", "def", "ghij").scan(10) { acc, elt -> acc + elt.length }
            .toList() shouldEqual listOf(10, 11, 13, 16, 20)
    }
}
