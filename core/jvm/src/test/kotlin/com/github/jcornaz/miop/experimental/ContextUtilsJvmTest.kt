package com.github.jcornaz.miop.experimental

import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.launch
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.assertNull

class ContextUtilsJvmTest {


    @Test
    fun `IoPool should dispatch on new thread when they are all busy`() = runTest {
        val threadNames = ConcurrentHashMap<String, Unit>()

        repeat(10) {
            launch(coroutineContext + IoPool) {
                Thread.sleep(500)
                assertNull(threadNames.put(Thread.currentThread().name, Unit))
                Thread.sleep(500)
            }
        }
    }
}
