package com.github.jcornaz.miop.javafx.experimental


internal inline fun <R> redirectExceptionToUncaughtExceptionHandler(block: () -> R): R? =
        try {
            block()
        } catch (t: Throwable) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t)
            null
        }
