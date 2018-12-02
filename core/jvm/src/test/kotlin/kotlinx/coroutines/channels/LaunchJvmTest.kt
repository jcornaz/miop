package kotlinx.coroutines.channels

import com.github.jcornaz.miop.operator.DummyException
import com.github.jcornaz.miop.test.delayTest
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class LaunchJvmTest {

    @Test
    fun exceptionHandlerAvoidTransmissionToUncaughtExceptionHandler() = runTest {
        var uncaughtException: Throwable? = null

        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaughtException = e }

        GlobalScope.launch(CoroutineExceptionHandler { _, _ -> /* ignore */ }) { throw DummyException() }.join()

        delayTest()

        uncaughtException.shouldBeNull()
    }
}