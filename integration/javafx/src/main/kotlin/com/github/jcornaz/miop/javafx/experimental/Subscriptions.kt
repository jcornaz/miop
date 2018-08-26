package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.collekt.api.PersistentList
import com.github.jcornaz.collekt.toPersistentList
import com.github.jcornaz.miop.experimental.awaitCancel
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.javafx.JavaFx

/**
 * Returns a [ReceiveChannel] through which all new value of this observable are sent.
 *
 * The result channel starts with the current value.
 */
public fun <T> ObservableValue<out T>.openValueSubscription(): ReceiveChannel<T?> = produce(JavaFx, Channel.CONFLATED) {
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
 */
public fun <E> ObservableList<out E>.openListSubscription(): ReceiveChannel<PersistentList<E>> = produce(JavaFx, Channel.CONFLATED) {
    var list: PersistentList<E> = toPersistentList().also { offer(it) }

    val listener = ListChangeListener<E> { change ->
        while (change.next()) {
            when {
                change.wasPermutated() -> {
                    val previous = list
                    for (oldIndex in (change.from until change.to)) {
                        list = list.with(change.getPermutation(oldIndex), previous[oldIndex])
                    }
                }
                change.wasUpdated() -> {
                    list = when {
                        change.to - change.from == 1 -> list.with(change.from, change.list[change.from])
                        change.from == 0 && change.to == list.size -> change.list.toPersistentList()
                        else -> list.subList(0, change.from) + change.list.subList(change.from, change.to) + list.subList(change.to, list.size)
                    }
                }
                change.wasRemoved() || change.wasAdded() -> {
                    list = list.subList(0, change.from) + change.addedSubList + list.subList(list.size - (change.list.size - change.to), list.size)
                }
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
