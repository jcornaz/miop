package com.github.jcornaz.miop.test

expect abstract class AsyncTest() {
    protected fun expect(step: Int)
    protected fun finish(step: Int)
    protected fun unreachable(createMessage: () -> String = { "Unreachable code reached" }): Nothing
}
