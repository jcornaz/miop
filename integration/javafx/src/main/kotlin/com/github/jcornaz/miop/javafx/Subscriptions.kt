package com.github.jcornaz.miop.javafx

import com.github.jcornaz.collekt.api.PersistentList
import com.github.jcornaz.collekt.toPersistentList
import com.github.jcornaz.miop.awaitCancel
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.javafx.JavaFx

/**
 * Returns a [ReceiveChannel] through which all new value of this observable are sent.
 *
 * The result channel starts with the current value.
 *
 * Result channel shall be cancelled to unsubscribe from the source.
 */
@ExperimentalCoroutinesApi
public fun <T> ObservableValue<out T>.openValueSubscription(): ReceiveChannel<T?> = GlobalScope.produce(Dispatchers.JavaFx, Channel.CONFLATED) {
    offer(value)

    val listener = ChangeListener<T?> { _, _, newValue -> offer(newValue) }

    addListener(listener)

    try {
        awaitCancel()
    } finally {
        removeListener(listener)
    }
}

/**
 * Returns a [ReceiveChannel] through which all the new list are sent each time it changes.
 *
 * The result channel starts with the current value.
 *
 * Result channel shall be cancelled to unsubscribe from the source.
 */
@ExperimentalCoroutinesApi
public fun <E> ObservableList<out E>.openListSubscription(): ReceiveChannel<PersistentList<E>> = GlobalScope.produce(Dispatchers.JavaFx, Channel.CONFLATED) {
    var list: PersistentList<E> = toPersistentList().also { offer(it) }

    val listener = ListChangeListener<E> { change ->
        while (change.next()) {
            when {
                change.wasPermutated() -> list = permute<E>(list, change)
                change.wasUpdated() -> list = update<E>(list, change)
                change.wasRemoved() || change.wasAdded() -> list = removeAndAdd<E>(list, change)
            }
        }
        offer(list)
    }

    addListener(listener)

    try {
        awaitCancel()
    } finally {
        removeListener(listener)
    }
}

private fun <E> permute(list: PersistentList<E>, change: ListChangeListener.Change<out E>): PersistentList<E> {
    var list1 = list
    val previous = list1
    for (oldIndex in (change.from until change.to)) {
        list1 = list1.with(change.getPermutation(oldIndex), previous[oldIndex])
    }
    return list1
}

private fun <E> update(list: PersistentList<E>, change: ListChangeListener.Change<out E>): PersistentList<E> {
    var list1 = list
    list1 = when {
        change.to - change.from == 1 -> list1.with(change.from, change.list[change.from])
        change.from == 0 && change.to == list1.size -> change.list.toPersistentList()
        else -> list1.subList(0, change.from) + change.list.subList(change.from, change.to) + list1.subList(change.to, list1.size)
    }
    return list1
}

private fun <E> removeAndAdd(list: PersistentList<E>, change: ListChangeListener.Change<out E>): PersistentList<E> {
    var list1 = list
    list1 = list1.subList(0, change.from) + change.addedSubList + list1.subList(list1.size - (change.list.size - change.to), list1.size)
    return list1
}
