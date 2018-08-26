package com.github.jcornaz.miop

import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.test.Test

class ContextUtilsTest {

    @Test
    fun itShouldBePossibleToLaunchCoroutinesOnIoPool() = runTest {
        repeat(10) {
            launch(coroutineContext + IoPool) { delay(100) }
        }
    }
}
