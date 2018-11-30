package com.github.jcornaz.miop.test

import kotlin.js.Date

actual inline fun measureTimeMillis(block: () -> Unit): Long {
    val started = Date.now()
    block()
    return (Date.now() - started).toLong()
}
