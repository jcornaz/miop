package com.github.jcornaz.miop.experimental

import kotlin.test.fail

inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T =
        try {
            block()
            null
        } catch (t: Throwable) {
            t as? T ?: fail("${T::class} was expected but ${t::class} was thrown")
        } ?: fail("${T::class} was expected but no exception was thrown")
