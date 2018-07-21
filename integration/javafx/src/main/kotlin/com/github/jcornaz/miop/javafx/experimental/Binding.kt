package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.collekt.api.PersistentList
import com.github.jcornaz.miop.experimental.distinctUntilChanged
import com.github.jcornaz.miop.experimental.launchConsumeEach
import com.github.jcornaz.miop.experimental.property.StateStore
import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.property.openSubscription
import javafx.beans.property.Property
import javafx.collections.ObservableList
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.coroutineContext

fun <T, R> Property<in R>.bind(
    subscribable: SubscribableValue<T>,
    parent: Job? = null,
    transform: (T) -> R
): Job = subscribable
    .openSubscription { transform(it) }
    .launchConsumeEach(JavaFx, parent = parent) { value = it }

fun <T> Property<in T>.bind(
    subscribable: SubscribableValue<T>,
    parent: Job? = null
): Job = bind(subscribable, parent) { it }

fun <E, T> ObservableList<in E>.bind(
    subscribable: SubscribableValue<T>,
    parent: Job? = null,
    transform: suspend (T) -> List<E>
): Job = subscribable
    .openSubscription { transform(it) }
    .launchConsumeEach(JavaFx, parent = parent) { setAll(it) }

fun <E> ObservableList<in E>.bind(
    subscribable: SubscribableValue<List<E>>,
    parent: Job? = null
): Job = bind(subscribable, parent) { it }

fun <S, A, R> Property<R>.bindBidirectional(
    store: StateStore<S, A>,
    parent: Job? = null,
    transformState: (S) -> R,
    createAction: (R?) -> A
): Job = launch(JavaFx, parent = parent) {
    store.openSubscription { transformState(it) }
        .distinctUntilChanged()
        .launchConsumeEach(JavaFx, CoroutineStart.UNDISPATCHED, coroutineContext[Job]) { value = it }

    openValueSubscription()
        .launchConsumeEach(parent = coroutineContext[Job]) { store.dispatch(createAction(it)) }
}

fun <S, A, E> ObservableList<E>.bindBidirectional(
    store: StateStore<S, A>,
    parent: Job? = null,
    transformState: (S) -> List<E>,
    createAction: (PersistentList<E>) -> A
): Job = launch(JavaFx, parent = parent) {
    store.openSubscription { transformState(it) }
        .launchConsumeEach(JavaFx, CoroutineStart.UNDISPATCHED, coroutineContext[Job]) { setAll(it) }

    openListSubscription()
        .launchConsumeEach(parent = coroutineContext[Job]) { store.dispatch(createAction(it)) }
}
