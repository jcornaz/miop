package com.github.jcornaz.miop.internal.test

import kotlinx.coroutines.experimental.promise

actual fun runTest(block: suspend () -> Unit): dynamic = promise { block() }
