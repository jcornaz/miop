package com.github.jcornaz.miop.internal.test

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

actual abstract class AsyncTest {
    private val stepCount = AtomicInteger()
    private val isFinished = AtomicBoolean()
    private var exception: Throwable? = null

    @BeforeTest
    fun setup() {
        stepCount.set(0)
        isFinished.set(false)
        exception = null

        Thread.setDefaultUncaughtExceptionHandler { _, t -> unreachable { "Exception \"$t\" delivered to the uncaught exception handler" } }
    }

    @AfterTest
    fun tearDown() {
        if (stepCount.get() > 0) assertTrue(isFinished.get(), "finish() was not called")
        exception?.let { throw it }
    }

    protected actual fun expect(step: Int) {
        assertFalse(isFinished.get(), "expect($step) has been called after finish(${stepCount.get()}")
        assertTrue(stepCount.incrementAndGet() == step, "unexpected step count (expected $step, but ${stepCount.get()} was found)")
    }

    protected actual fun finish(step: Int) {
        assertTrue(isFinished.compareAndSet(false, true), "finish has been called twice")
        assertTrue(stepCount.incrementAndGet() == step, "unexpected step count (expected $step, but ${stepCount.get()} was found)")
    }

    protected actual fun unreachable(createMessage: () -> String) {
        exception = AssertionError(createMessage())
    }
}
