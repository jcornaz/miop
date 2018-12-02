package com.github.jcornaz.miop.operator

import com.github.jcornaz.miop.receiveChannelOf
import com.github.jcornaz.miop.switchMap
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwitchMapTest : OperatorTest() {
    override fun <T> ReceiveChannel<T>.identityOperation(): ReceiveChannel<T> =
        switchMap { receiveChannelOf(it) }

    @Test
    fun shouldEmitItemsOfTheNewSourceOnly() = runTest {
        val sources = (0..2).map { Channel<Char>(2) }
        val switch = Channel<Int>()
        val result = switch.switchMap { sources[it] }

        expect(1)
        launch(Dispatchers.Unconfined) {
            expect(2)
            assertEquals('a', result.receive())
            assertEquals('b', result.receive())
            expect(5)
            assertEquals('c', result.receive())
            assertEquals('d', result.receive())
            expect(7)
            assertEquals('y', result.receive())
            expect(9)
            assertEquals('e', result.receive())
            assertEquals('f', result.receive())
            expect(11)
            assertThrows<ClosedReceiveChannelException> { result.receive() }
            expect(14)
        }

        expect(3)
        sources[0].send('a')
        sources[0].send('b')
        sources[1].send('c')
        sources[1].send('d')
        sources[2].send('e')
        sources[2].send('f')
        expect(4)
        switch.send(0) // start the first source
        expect(6)
        switch.send(1) // start the second source
        expect(8)
        assertThrows<Exception> { sources[0].send('x') } // the first source should have been cancelled
        sources[1].send('y')
        expect(10)
        sources[1].close() // should have no impact
        switch.send(2) // start the third source
        expect(12)
        switch.close() // should have no impact
        expect(13)
        sources[2].close() // should close the result as the switch is closed
        finish(15)
    }

    @Test
    fun ifAOpenedSourceFailsTheResultShouldFailWithTheSameException() = runTest {
        val source = Channel<Int>()
        val result = receiveChannelOf(1).switchMap { source }

        source.close(Exception("my exception"))

        assertTrue(result.isClosedForReceive)
        assertEquals("my exception", assertThrows<Exception> { result.receive() }.message)
    }

    @Test
    fun cancellingShouldCancelTheCurrentSource() = runTest {
        val source = Channel<Int>()
        val result = receiveChannelOf(1).switchMap { source }

        result.cancel()

        assertTrue(source.isClosedForReceive)
        assertThrows<Exception> { source.receive() }
    }
}
