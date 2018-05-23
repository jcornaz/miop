package com.github.jcornaz.miop.experimental

import com.github.jcornaz.miop.internal.test.AsyncTest
import com.github.jcornaz.miop.internal.test.assertThrows
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import kotlin.test.Test

class SuspensionTest : AsyncTest() {

    @Test
    fun `awaitCancel should suspend until the cancellation of the coroutines`() {
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
    fun `awaitCancel should throw if the coroutines is already cancelled`() {
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
