package com.github.jcornaz.miop.test

actual inline fun measureTimeMillis(block: () -> Unit): Long = kotlin.system.measureTimeMillis(block)