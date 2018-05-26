package com.github.jcornaz.miop.internal.test

import com.github.jcornaz.miop.internal.test.ManualTimer.TimeerCommand.*
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import kotlin.coroutines.experimental.Continuation


actual class ManualTimer {
    private val actor = actor<TimeerCommand>(Unconfined, Channel.UNLIMITED) {

        val pendingContinuations = mutableMapOf<Int, MutableCollection<Continuation<Unit>>>()
        var currentTime = 0

        try {
            consumeEach { command ->
                when (command) {
                    is Await -> {
                        if (command.time <= currentTime) {
                            command.continuation.resume(Unit)
                        } else {
                            pendingContinuations.getOrPut(command.time) { mutableListOf() }.add(command.continuation)
                        }
                    }
                    is AdvanceTo -> {
                        if (command.time > currentTime) {
                            currentTime = command.time
                            pendingContinuations.keys.filter { it <= command.time }.forEach {
                                pendingContinuations.remove(it)?.forEach { it.resume(Unit) }
                            }
                        }
                    }
                    is Terminate -> {
                        if (pendingContinuations.isEmpty()) {
                            command.continuation.resume(Unit)
                        } else {
                            command.continuation.resumeWithException(AssertionError("The timer has been terminated before all suspended code was resumed"))
                        }
                        return@actor
                    }
                }
            }
        } finally {
            pendingContinuations.forEach { (time, continuations) ->
                continuations.forEach { it.resumeWithException(AssertionError("Unreached time: $time")) }
            }
            pendingContinuations.clear()
        }
    }

    actual suspend fun await(time: Int) = suspendCancellableCoroutine<Unit> { continuation ->
        actor.offer(Await(time, continuation))
    }

    actual fun advanceTo(time: Int) {
        actor.offer(AdvanceTo(time))
    }

    actual suspend fun terminate() = suspendCancellableCoroutine<Unit> { continuation ->
        actor.offer(Terminate(continuation))
    }

    private sealed class TimeerCommand {
        class Await(val time: Int, val continuation: Continuation<Unit>) : TimeerCommand()
        class AdvanceTo(val time: Int) : TimeerCommand()
        class Terminate(val continuation: Continuation<Unit>) : TimeerCommand()
    }
}
