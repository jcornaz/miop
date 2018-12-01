package com.github.jcornaz.miop.test

import kotlin.test.*

actual abstract class AsyncTest actual constructor() {
    private var stepCount = 0
    private var isFinished = false
    private var exception: Throwable? = null

    @BeforeTest
    fun setup() {
        stepCount = 0
        isFinished = false
        exception = null
    }

    @AfterTest
    fun tearDown() {
        if (stepCount > 0) assertTrue(isFinished, "finish() was not called")
        exception?.let { throw it }
    }

    protected actual fun expect(step: Int) {
        assertFalse(isFinished, "expect($step) has been called after finish($stepCount")
        assertEquals(++stepCount, step, "unexpected step count (expected $step, but $stepCount was found)")
    }

    protected actual fun finish(step: Int) {
        assertTrue(isFinished, "finish has been called twice")
        assertEquals(++stepCount, step, "unexpected step count (expected $step, but $stepCount was found)")
    }

    protected actual fun unreachable(createMessage: () -> String): Nothing {
        val error = AssertionError(createMessage())
        exception = error
        throw error
    }
}