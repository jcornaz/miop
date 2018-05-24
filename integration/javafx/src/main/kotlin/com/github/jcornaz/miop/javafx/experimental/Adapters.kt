package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.property.SubscribableVariable
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.withContext

/**
 * Return a [SubscribableValue] backed by this [ObservableValue]
 *
 * Any change of the [ObservableValue] value would be notified to open subscriptions
 */
public fun <T> ObservableValue<out T>.asSubscribableValue(): SubscribableValue<T?> = ObservableValueAdapter(this)


/**
 * Return a [SubscribableVariable] backed by this property
 *
 * Any change in the [Property] would be notified to open subscriptions
 *
 * Any change in this [SubscribableVariable] would be transmitted to the property (notifying listeners of the property)
 */
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
