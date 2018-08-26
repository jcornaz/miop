package com.github.jcornaz.miop.property

import kotlinx.coroutines.Unconfined
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Object which store state and accept events in order to get new states.
 *
 * There is only one state at a time, and the state is supposed to be immutable.
 *
 * It is possible to get a subscription of the states with [openSubscription]
 *
 * This concept is heavily inspired from [redux](https://redux.js.org).
 */
public interface StateStore<out S, in E> : SubscribableValue<S> {

    /**
     * Handle an event in order to mutate the state.
     *
     * May suspend until the previous events have been processed
     *
     * @return The state resulting of applying this event
     */
    suspend fun handle(event: E): S
}

/** Dispatch an event in order to mutate the state. The event may be scheduled for later */
fun <E> StateStore<*, E>.dispatch(event: E) {
    launch(Unconfined) { handle(event) }
}

/**
 * Create a [StateStore] with the [initialState].
 *
 * @param initialState Initial state of the store
 */
public fun <S, E : (S) -> S> StateStore(initialState: S): StateStore<S, E> = StateStore(initialState) { state, event -> event(state) }

/**
 * Create a [StateStore] with the [initialState] and a [reducer].
 *
 * @param initialState Initial state of the store
 * @param reducer Function called for each dispatched event and responsible to return a new state. Should handle events in a fast and non-blocking manner.
 */
public fun <S, E> StateStore(initialState: S, reducer: (state: S, event: E) -> S): StateStore<S, E> = SimpleStateStore(initialState, reducer)

/**
 * Returns a [StateStore] which is a *view* on this store, transforming the state from it with [transformState] and delegating events transformed by [transformEvent]
 */
public fun <S1, S2, E1, E2> StateStore<S1, E1>.map(
    transformState: (S1) -> S2,
    transformEvent: (E2) -> E1
): StateStore<S2, E2> = StateStoreView(this, transformState, transformEvent)

private class SimpleStateStore<out S, in A>(
    initialState: S,
    private val reducer: (state: S, event: A) -> S
) : StateStore<S, A> {

    private val broadcast = ConflatedBroadcastChannel(initialState)
    private val mutex = Mutex()

    override suspend fun get(): S = broadcast.value

    override suspend fun handle(event: A): S = mutex.withLock {
        val previousState = broadcast.value
        reducer(previousState, event).also { if (it !== previousState) broadcast.send(it) }
    }

    override fun openSubscription(): ReceiveChannel<S> = broadcast.openSubscription()
}

private class StateStoreView<in S1, out S2, out E1, in E2>(
    private val origin: StateStore<S1, E1>,
    private val transformState: (S1) -> S2,
    private val transformEvent: (E2) -> E1
) : StateStore<S2, E2> {

    override suspend fun get(): S2 = transformState(origin.get())

    override suspend fun handle(event: E2): S2 = transformState(origin.handle(transformEvent(event)))

    override fun openSubscription(): ReceiveChannel<S2> =
        origin.openSubscription { transformState(it) }
}
