package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.scan
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.toList
import org.amshove.kluent.shouldEqual
import kotlin.test.Test

class ScanTest : OperatorTest() {

    override fun <T : Any> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        scan { _, it: T -> it }

    @Test
    fun shouldSendAccumulation() = runTest {
        receiveChannelOf(1, 2, 3, 4).scan { acc, elt -> acc + elt }
            .toList() shouldEqual listOf(1, 3, 6, 10)
    }
}
