package com.github.jcornaz.miop.internal.test

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock

class ManualTimer {

    private val mutex = Mutex(false)
    private val pendingContinuations = mutableMapOf<Int, MutableCollection<CompletableDeferred<Unit>>>()
    private var currentTime = 0

    suspend fun await(time: Int) {
        val deferred = CompletableDeferred<Unit>()

        mutex.withLock {
            if (time <= currentTime) {
                deferred.complete(Unit)
            } else {
                pendingContinuations.getOrPut(time) { mutableListOf() }.add(deferred)
            }
        }

        deferred.await()
    }

    fun advanceTo(time: Int) {
        launch(Unconfined) {
            mutex.withLock {
                if (time > currentTime) {
                    currentTime = time
                    pendingContinuations.keys.filter { it <= time }.forEach {
                        pendingContinuations.remove(it)?.forEach { it.complete(Unit) }
                    }
                }
            }
        }
    }

    suspend fun terminate() {
        mutex.withLock {
            if (!pendingContinuations.isEmpty()) {
                pendingContinuations.entries.forEach { (time, continuations) ->
                    continuations.forEach { it.completeExceptionally(AssertionError("Unreached time: $time")) }
                }
                pendingContinuations.clear()
                throw AssertionError("The timer has been terminated before all suspended code was resumed")
            }
        }
    }

}
