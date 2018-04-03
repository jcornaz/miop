package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.awaitCancel
import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.property.SubscribableVariable
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.cancel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import java.lang.ref.WeakReference

public fun <T> SubscribableVariable<in T>.bind(observable: ObservableValue<T?>, nullValue: T): Job = launch(JavaFx) {
    val ref = WeakReference(this@bind)

    val listener = ChangeListener<T?> { _, _, newValue ->
        ref.get()?.let { it.value = newValue ?: nullValue } ?: coroutineContext.cancel()
    }

    observable.addListener(listener)

    try {
        awaitCancel()
    } finally {
        observable.removeListener(listener)
    }
}

public fun <T> SubscribableVariable<in T?>.bind(observable: ObservableValue<T?>): Job = bind(observable, null)

public fun <T> Property<in T>.bind(channel: ReceiveChannel<T>): Job = launch(JavaFx) {
    val ref = WeakReference(this@bind)

    channel.consumeEach { newValue ->
        ref.get()?.let { it.value = newValue } ?: throw CancellationException()
    }
}

public fun <T> Property<in T>.bind(subscribable: SubscribableValue<T>): Job = bind(subscribable.openSubscription())
