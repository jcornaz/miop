package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.awaitCancel
import com.github.jcornaz.miop.experimental.launchConsumeEach
import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.property.SubscribableVariable
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch

fun <T> SubscribableVariable<in T>.bind(source: ObservableValue<out T>, parent: Job? = null): Job = launch(JavaFx, parent = parent) {
    val listener = ChangeListener<T> { _, _, newValue ->
        value = newValue
    }

    source.addListener(listener)

    try {
        awaitCancel()
    } finally {
        source.removeListener(listener)
    }
}

fun <T> Property<in T>.bind(source: SubscribableValue<T>, parent: Job? = null): Job =
        bind(source.openSubscription(), parent)

fun <T> Property<in T>.bind(source: ReceiveChannel<T>, parent: Job? = null): Job =
        source.launchConsumeEach(JavaFx, parent = parent) { value = it }
