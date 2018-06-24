package com.github.jcornaz.miop.experimental

import com.github.jcornaz.miop.internal.test.runTest
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.coroutineContext
import kotlin.test.Test

class ContextUtilsTest {

    @Test
    fun itShouldBePossibleToLaunchCoroutinesOnIoPool() = runTest {
        repeat(10) {
            launch(coroutineContext + IoPool) { delay(100) }
        }
    }
}
