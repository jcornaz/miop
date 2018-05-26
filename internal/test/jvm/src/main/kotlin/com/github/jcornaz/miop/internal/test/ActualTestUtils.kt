package com.github.jcornaz.miop.internal.test

import kotlinx.coroutines.experimental.runBlocking

actual fun runTest(block: suspend () -> Unit) = runBlocking { block() }
