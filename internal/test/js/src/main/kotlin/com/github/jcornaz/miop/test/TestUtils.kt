package com.github.jcornaz.miop.test

import kotlinx.coroutines.promise

actual fun runTest(block: suspend () -> Unit): dynamic = promise { block() }
