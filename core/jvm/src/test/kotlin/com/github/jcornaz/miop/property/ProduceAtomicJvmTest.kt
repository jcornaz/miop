package com.github.jcornaz.miop.property

import com.github.jcornaz.miop.operator.DummyException
import com.github.jcornaz.miop.produceAtomic
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.delayTest
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.first
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class ProduceAtomicJvmTest {

    @Test
    fun shouldNotTransmitToUncaughtExceptionHandler() = runTest {
        var uncaughtException: Throwable? = null

        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaughtException = e }

        assertThrows<DummyException> {
            GlobalScope.produceAtomic<String>(Dispatchers.Unconfined) { throw DummyException() }.first()
        }

        delayTest()

        uncaughtException.shouldBeNull()
    }
}
