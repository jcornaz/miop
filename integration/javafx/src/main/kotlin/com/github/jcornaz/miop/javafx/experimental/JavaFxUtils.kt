package com.github.jcornaz.miop.javafx.experimental

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

public fun <T> ObservableValue<out T>.openValueSubscription(): ReceiveChannel<T?> {
    val channel = Channel<T?>(Channel.CONFLATED)
    val listener = ChangeListener<T?> { _, _, newValue -> channel.offer(newValue) }

    addListener(listener)

    return object : ReceiveChannel<T?> by channel {
        override fun cancel(cause: Throwable?): Boolean {
            removeListener(listener)
            return channel.cancel(cause)
        }
    }
}
