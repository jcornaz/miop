package com.github.jcornaz.miop

import com.github.jcornaz.miop.test.AsyncTest
import com.github.jcornaz.miop.test.assertThrows
import com.github.jcornaz.miop.test.runTest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.test.Test

class SuspensionTest : AsyncTest() {

    @Test
    fun awaitCancelShouldSuspendUntilTheCancellationOfTheCoroutines() = runTest {
        expect(1)
        val job = launch(Dispatchers.Unconfined) {
            expect(2)
            assertThrows<CancellationException> { awaitCancel() }
            expect(4)
        }
        expect(3)
        job.cancel()
        finish(5)
    }

    @Test
    fun awaitCancelShouldThrowIfTheCoroutinesIsAlreadyCancelled() = runTest {
        expect(1)
        launch(Dispatchers.Unconfined) {
            expect(2)
            coroutineContext[Job]!!.cancel()
            assertThrows<CancellationException> { awaitCancel() }
            expect(3)
        }
        finish(4)
    }
}
