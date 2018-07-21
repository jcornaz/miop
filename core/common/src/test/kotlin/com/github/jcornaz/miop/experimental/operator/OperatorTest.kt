package com.github.jcornaz.miop.experimental.operator

import com.github.jcornaz.miop.internal.test.assertThrows
import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlin.test.Ignore
import kotlin.test.Test

abstract class OperatorTest {

    abstract fun ReceiveChannel<Int>.operator(): ReceiveChannel<Int>

    @Test
    @Ignore // wait on https://github.com/Kotlin/kotlinx.coroutines/issues/415
    fun testCancel() = runTest {
        val source = Channel<Int>()
        source.operator().cancel(DummyException("something went wrong"))

        assertThrows<Exception> { source.send(0) }
    }
}