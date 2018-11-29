package kotlinx.coroutines.channels

import com.github.jcornaz.miop.operator.DummyException
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.delayTest
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.*
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class ChannelJvmTest {

    @Test
    fun produceDoesNotTransmitErrorToUncaughtExceptionHandler() = runTest {
        var uncaughtException: Throwable? = null

        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaughtException = e }

        assertThrows<DummyException> {
            GlobalScope.produce<String>(Dispatchers.Unconfined) { throw DummyException() }.first()
        }

        delayTest()

        uncaughtException.shouldBeNull()
    }

    @Test
    fun errorInMapIsNotTransmittedToUncaughtExceptionHandler() = runTest {
        var uncaughtException: Throwable? = null

        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaughtException = e }

        assertThrows<DummyException> {
            GlobalScope.produce(Dispatchers.Unconfined) { send(1) }.map { throw DummyException() }.consumeEach { }
        }

        delayTest()

        uncaughtException.shouldBeNull()
    }

    @Test
    fun globalScopeTransmitToUncaughtExceptionHandler() = runTest {
        val uncaughtException = CompletableDeferred<Throwable>()

        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaughtException.complete(e) }

        GlobalScope.launch(Dispatchers.Unconfined) { throw DummyException() }

        withTimeout(1000) {
            uncaughtException.await()
        }
    }
}
