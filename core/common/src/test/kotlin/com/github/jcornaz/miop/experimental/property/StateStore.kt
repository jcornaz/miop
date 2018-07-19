package com.github.jcornaz.miop.experimental.property

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch

public interface StateStore<S, in A> : SubscribableValue<S> {
    fun dispatch(action: A)
}

public class SimpleStateStore<S, in A : (S) -> S>(
        initialState: S,
        onError: (error: Throwable) -> A
) : StateStore<S, A> {

    private val broadcast = ConflatedBroadcastChannel(initialState)
    private val pendingActions = Channel<A>(Channel.UNLIMITED)

    init {
        launch(Unconfined) {
            var state = initialState

            pendingActions.consumeEach { action ->
                val newState = try {
                    action(state)
                } catch (error: Throwable) {
                    onError(error)(state)
                }

                if (newState !== state) {
                    broadcast.send(newState)
                    state = newState
                }
            }
        }
    }

    override suspend fun get(): S = broadcast.value

    override fun dispatch(action: A) {
        pendingActions.offer(action)
    }

    override fun openSubscription(): ReceiveChannel<S> = broadcast.openSubscription()
}
