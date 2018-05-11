package com.github.jcornaz.miop.experimental

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import kotlin.test.Test
import kotlin.test.assertTrue


class PrimitivesTest : AsyncTest() {

    @Test
    fun `awaitCancel should suspend until the cancellation of the coroutines`() {
        expect(1)
        val job = launch(Unconfined) {
            expect(2)
            try {
                awaitCancel() // should suspend

                @Suppress("UNREACHABLE_CODE") // assert that it is unreachable
                unreachable()
            } catch (t: Throwable) {
                assertTrue { t is CancellationException }
            }
            expect(4)
        }
        expect(3)
        job.cancel()
        finish(5)
    }
}
