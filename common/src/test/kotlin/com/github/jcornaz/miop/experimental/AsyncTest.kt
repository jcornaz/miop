package com.github.jcornaz.miop.experimental

expect abstract class AsyncTest() {
    protected fun expect(step: Int)
    protected fun finish(step: Int)
    protected fun unreachable(createMessage: () -> String = { "Unreachable code reached" })
}
