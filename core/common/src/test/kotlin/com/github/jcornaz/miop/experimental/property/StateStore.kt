package com.github.jcornaz.miop.experimental.property

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.launch

public interface StateStore<S, in A : (S) -> S> : SubscribableValue<S> {
    fun dispatch(action: A)
}

public fun <S, A : (S) -> S> StateStore(
        initialState: S,
        dispatchError: (S, Throwable) -> S
): StateStore<S, A> = StateStoreImpl(initialState, dispatchError)

private class StateStoreImpl<S, in A : (S) -> S>(
        initialState: S,
        private val dispatchError: (S, Throwable) -> S
) : StateStore<S, A> {

    private val broadcast = ConflatedBroadcastChannel(initialState)

    private val pendingActions = Channel<A>(Channel.UNLIMITED)

    init {
        launch(Unconfined) {
            var state = initialState

            pendingActions.consumeEach {
                try {
                    val newState = it(state)
                    if (newState !== state) {
                        broadcast.send(newState)
                        state = newState
                    }
                } catch (t: Throwable) {
                    state = dispatchError(state, t)
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

private class StateStoreMapImpl<SS, SR, AS : (SS) -> SS, AR : (SR) -> SR>(
        private val sourceStore: StateStore<SS, AS>,
        private val transformState: (SS) -> SR,
        private val transformAction: (AR) -> AS
) : StateStore<SR, AR> {

    override fun dispatch(action: AR) = sourceStore.dispatch(transformAction(action))

    override suspend fun get(): SR = transformState(sourceStore.get())

    override fun openSubscription(): ReceiveChannel<SR> =
            sourceStore.openSubscription()
                    .map { transformState(it) }
}

public fun <SS, SR, AS : (SS) -> SS, AR : (SR) -> SR> StateStore<SS, AS>.map(
        transformState: (SS) -> SR,
        transformAction: (AR) -> AS
): StateStore<SR, AR> = StateStoreMapImpl(this, transformState, transformAction)
