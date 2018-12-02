package com.github.jcornaz.miop

internal actual val defaultParallelism: Int get() = Runtime.getRuntime().availableProcessors()
