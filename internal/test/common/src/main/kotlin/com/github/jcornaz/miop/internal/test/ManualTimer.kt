package com.github.jcornaz.miop.internal.test

expect class ManualTimer {
    suspend fun await(time: Int)

    fun advanceTo(time: Int = 1)

    suspend fun terminate()
}
