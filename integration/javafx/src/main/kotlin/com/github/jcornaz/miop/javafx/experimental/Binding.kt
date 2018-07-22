package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.collekt.api.PersistentList
import com.github.jcornaz.miop.experimental.launchConsumeEach
import com.github.jcornaz.miop.experimental.property.StateStore
import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.property.openSubscription
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.javafx.JavaFx

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

fun <S, A, T> StateStore<S, A>.bind(
    observableValue: ObservableValue<T>,
    parent: Job? = null,
    createAction: (T?) -> A
): Job = observableValue.openValueSubscription()
    .launchConsumeEach(parent = parent) { dispatch(createAction(it)) }

fun <S, A, E> StateStore<S, A>.bind(
    observableList: ObservableList<out E>,
    parent: Job? = null,
    createAction: (PersistentList<E>) -> A
): Job = observableList.openListSubscription()
    .launchConsumeEach(parent = parent) { dispatch(createAction(it)) }
