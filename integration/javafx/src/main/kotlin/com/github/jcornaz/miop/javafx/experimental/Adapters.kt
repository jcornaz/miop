package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.property.SubscribableVariable
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.withContext

public fun <T> ObservableValue<out T>.asSubscribableValue(): SubscribableValue<T?> = ObservableValueAdapter(this)
public fun <T> Property<T>.asSubscribableVariable(): SubscribableVariable<T?> = PropertyAdapter(this)

private class ObservableValueAdapter<out T>(private val observable: ObservableValue<out T>) : SubscribableValue<T?> {
    override suspend fun get(): T? = withContext(JavaFx) { observable.value }

    override fun openSubscription() = observable.openValueSubscription()
}

private class PropertyAdapter<T>(private val property: Property<T>) : SubscribableVariable<T?> {

    override suspend fun get(): T? = withContext(JavaFx) { property.value }

    override suspend fun set(value: T?) = withContext(JavaFx) {
        property.value = value
    }

    override fun openSubscription() = property.openValueSubscription()
}
