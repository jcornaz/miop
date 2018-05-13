package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.property.SubscribableVariable
import com.github.jcornaz.miop.experimental.property.bind
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.Job

fun <T> SubscribableVariable<in T?>.bind(source: ObservableValue<out T>, parent: Job? = null): Job =
        bind(source.openValueSubscription(), parent)

fun <T> Property<in T>.bind(source: SubscribableValue<T>) =
        bind(source.asObservableValue())

fun <T> Property<T>.bindBidirectional(other: SubscribableVariable<T?>) =
        bindBidirectional(other.asProperty())
