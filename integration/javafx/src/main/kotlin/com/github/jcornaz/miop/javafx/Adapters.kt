package com.github.jcornaz.miop.javafx

import com.github.jcornaz.miop.property.ExperimentalSubscribable
import com.github.jcornaz.miop.property.SubscribableValue
import com.github.jcornaz.miop.property.SubscribableVariable
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext

/**
 * Return a [SubscribableValue] backed by this [ObservableValue]
 *
 * Any change of the [ObservableValue] value would be notified to open subscriptions
 */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("Use updaters and subscriptions instead")
@UseExperimental(ExperimentalSubscribable::class)
public fun <T> ObservableValue<out T>.asSubscribableValue(): SubscribableValue<T?> = ObservableValueAdapter(this)

/**
 * Return a [SubscribableVariable] backed by this property
 *
 * Any change in the [Property] would be notified to open subscriptions
 *
 * Any change in this [SubscribableVariable] would be transmitted to the property (notifying listeners of the property)
 */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("Use updaters and subscriptions instead")
@UseExperimental(ExperimentalSubscribable::class)
public fun <T> Property<T>.asSubscribableVariable(): SubscribableVariable<T?> = PropertyAdapter(this)

@ExperimentalSubscribable
private class ObservableValueAdapter<out T>(private val observable: ObservableValue<out T>) : SubscribableValue<T?> {
    override suspend fun get(): T? = withContext(Dispatchers.JavaFx) { observable.value }

    override fun openSubscription() = observable.openValueSubscription()
}

@ExperimentalSubscribable
private class PropertyAdapter<T>(private val property: Property<T>) : SubscribableVariable<T?> {

    override suspend fun get(): T? = withContext(Dispatchers.JavaFx) { property.value }

    override suspend fun set(value: T?) = withContext(Dispatchers.JavaFx) {
        property.value = value
    }

    override fun openSubscription() = property.openValueSubscription()
}
