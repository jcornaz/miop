package kotlinx.coroutines.channels

import com.github.jcornaz.miop.operator.DummyException
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class ChannelJvmTest {

    @Test
    fun errorInMapIsNotTransmittedToUncaughtExceptionHandler() = runTest {
        var uncaughtException: Throwable? = null

        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaughtException = e }

        assertThrows<DummyException> {
            GlobalScope.produce { send(1) }.map { throw DummyException() }.consumeEach { }
        }

        delay(200)

        uncaughtException.shouldBeNull()
    }
}