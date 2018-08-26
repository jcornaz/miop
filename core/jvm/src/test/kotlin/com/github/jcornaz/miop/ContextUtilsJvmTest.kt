package com.github.jcornaz.miop

import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.launch
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext
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
