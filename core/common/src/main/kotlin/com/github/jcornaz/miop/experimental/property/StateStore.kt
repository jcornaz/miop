package com.github.jcornaz.miop.experimental.property

import kotlinx.coroutines.experimental.DefaultDispatcher
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
 * It is possible to get a subscription of the states with [openSubscription]
 *
 * This concept is heavily inspired from [redux](https://redux.js.org).
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
public fun <S, A : (S) -> S> StateStore(initialState: S): StateStore<S, A> = StateStore(initialState) { state, action -> action(state) }

/**
 * Create a [StateStore] with the [initialState] and a [reducer].
 *
 * @param initialState Initial state of the store
 * @param reducer Function called for each dispatched action and responsible to return a new state.
 */
public fun <S, A> StateStore(initialState: S, reducer: (state: S, action: A) -> S): StateStore<S, A> = SimpleStateStore(initialState, reducer)

/**
 * Returns a [StateStore] which is a *view* on this store, transforming the state from it with [transformState] and delegating actions transformed by [transformAction]
 */
public fun <S1, S2, A1, A2> StateStore<S1, A1>.map(
    transformState: (S1) -> S2,
    transformAction: (A2) -> A1
): StateStore<S2, A2> = StateStoreView(this, transformState, transformAction)

private class SimpleStateStore<out S, in A>(
    initialState: S,
    reducer: (state: S, action: A) -> S
) : StateStore<S, A> {

    private val broadcast = ConflatedBroadcastChannel(initialState)
    private val pendingActions = Channel<A>(Channel.UNLIMITED)

    init {
        launch(DefaultDispatcher) {
            var state = initialState

            pendingActions.consumeEach { action ->
                val newState = try {
                    reducer(state, action)
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

private class StateStoreView<in S1, out S2, out A1, in A2>(
    private val origin: StateStore<S1, A1>,
    private val transformState: (S1) -> S2,
    private val transformAction: (A2) -> A1
) : StateStore<S2, A2> {

    override suspend fun get(): S2 = transformState(origin.get())

    override fun dispatch(action: A2) = origin.dispatch(transformAction(action))

    override fun openSubscription(): ReceiveChannel<S2> =
        origin.openSubscription { transformState(it) }
}
