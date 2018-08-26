package com.github.jcornaz.miop.experimental

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.assertThrows
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.Unconfined
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.test.Test

class SuspensionTest : AsyncTest() {

    @Test
    fun awaitCancelShouldSuspendUntilTheCancellationOfTheCoroutines() {
        expect(1)
        val job = launch(Unconfined) {
            expect(2)
            assertThrows<CancellationException> { awaitCancel() }
            expect(4)
        }
        expect(3)
        job.cancel()
        finish(5)
    }

    @Test
    fun awaitCancelShouldThrowIfTheCoroutinesIsAlreadyCancelled() {
        expect(1)
        launch(Unconfined) {
            expect(2)
            coroutineContext[Job]!!.cancel()
            assertThrows<CancellationException> { awaitCancel() }
            expect(3)
        }
        finish(4)
    }
}
