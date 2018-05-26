package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.awaitCancel
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.javafx.JavaFx

/**
 * Return a receive channel through which all new value of this observable are sent.
 *
 * The result channel start with the current value.
 */
public fun <T> ObservableValue<out T>.openValueSubscription(): ReceiveChannel<T?> = produce(JavaFx, Channel.CONFLATED) {
    val listener = ChangeListener<T?> { _, _, newValue -> offer(newValue) }

    offer(value)

    addListener(listener)

    try {
        awaitCancel()
    } finally {
        removeListener(listener)
    }
}
