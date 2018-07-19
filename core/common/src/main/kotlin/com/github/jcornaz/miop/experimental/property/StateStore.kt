package com.github.jcornaz.miop.experimental.property

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

/**
 * Object which store state and accept actions in order to get new states.
 *
 * There is only one state at a time, and the state is supposed to be immutable.
 * Action should be fast, non-blocking, and preferably be pure.
 *
 * It is possible to get a subscription of state with [openSubscription]
 */
public interface StateStore<out S, in A> : SubscribableValue<S> {

    /** Dispatch an action in order to mutate the state. The action may be scheduled for later */
    fun dispatch(action: A)
}

/**
 * Create a [StateStore] with the [initialState].
 *
 * @param initialState Initial state of the store
 */
public fun <S, A : (S) -> S> StateStore(initialState: S): StateStore<S, A> = SimpleStateStore(initialState)

internal class SimpleStateStore<S, in A : (S) -> S>(initialState: S) : StateStore<S, A> {

    private val broadcast = ConflatedBroadcastChannel(initialState)
    private val pendingActions = Channel<A>(Channel.UNLIMITED)

    init {
        launch(Unconfined) {
            var state = initialState

            pendingActions.consumeEach { action ->
                val newState = try {
                    action(state)
                } catch (error: Throwable) {
                    launch(Unconfined) { throw error }
                    return@consumeEach
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
