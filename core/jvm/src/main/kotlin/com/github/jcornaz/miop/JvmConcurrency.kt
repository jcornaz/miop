package com.github.jcornaz.miop

internal actual val defaultConcurrency: Int get() = Runtime.getRuntime().availableProcessors()
