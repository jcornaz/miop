package com.github.jcornaz.miop.javafx.experimental

import com.github.jcornaz.miop.experimental.property.SubscribableValue
import com.github.jcornaz.miop.experimental.property.SubscribableVariable
import com.github.jcornaz.miop.experimental.property.bindBidirectional
import javafx.beans.InvalidationListener
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch

public fun <T> SubscribableValue<T>.asObservableValue(): ObservableValue<out T> = SubscribableValueAdapter(this)
public fun <T> SubscribableVariable<T?>.asProperty(): Property<T> = SubscribableVariableAdapter(this)
public fun <T> ObservableValue<out T>.asSubscribableValue(): SubscribableValue<T?> = ObservableValueAdapter(this)
public fun <T> Property<T>.asSubscribableVariable(): SubscribableVariable<T?> = PropertyAdapter(this)

private class ObservableValueAdapter<out T>(private val observable: ObservableValue<out T>) : SubscribableValue<T?> {
    override val value: T? get() = observable.value
    override fun openSubscription() = observable.openValueSubscription()
}

private class PropertyAdapter<T>(private val property: Property<T>) : SubscribableVariable<T?> {

    override var value: T?
        get() = property.value
        set(value) {
            property.value = value
        }

    override fun openSubscription() = property.openValueSubscription()
}

private class SubscribableValueAdapter<T>(private val subscribable: SubscribableValue<T?>) : ObservableValue<T> {

    private val invalidationListeners = mutableMapOf<InvalidationListener, Job>()
    private val changeListeners = mutableMapOf<ChangeListener<in T>, Job>()

    override fun addListener(listener: InvalidationListener) {
        if (listener in invalidationListeners) return

        invalidationListeners[listener] = launch(JavaFx) {
            subscribable.openSubscription().consumeEach {
                redirectExceptionToUncaughtExceptionHandler {
                    listener.invalidated(this@SubscribableValueAdapter)
                }
            }
        }
    }

    override fun removeListener(listener: InvalidationListener) {
        invalidationListeners.remove(listener)?.cancel()
    }

    override fun addListener(listener: ChangeListener<in T>) {
        if (listener in changeListeners) return

        changeListeners[listener] = launch(JavaFx) {
            subscribable.openSubscription().consume {
                var oldValue: T? = receive()
                for (newValue in this) {
                    redirectExceptionToUncaughtExceptionHandler {
                        listener.changed(this@SubscribableValueAdapter, oldValue, newValue)
                    }
                    oldValue = newValue
                }
            }
        }
    }

    override fun removeListener(listener: ChangeListener<in T>) {
        changeListeners.remove(listener)?.cancel()
    }

    override fun getValue(): T? = subscribable.value
}

private class SubscribableVariableAdapter<T>(private val subscribable: SubscribableVariable<T?>) : Property<T>, ObservableValue<T> by SubscribableValueAdapter(subscribable) {
    private var binding: Job? = null
    private val bidirectionalBindings = mutableMapOf<Property<T>, Job>()

    override fun setValue(value: T) {
        subscribable.value = value
    }

    override fun getName() = ""
    override fun getBean() = null

    override fun bindBidirectional(other: Property<T>) {
        require(other !== this)
        if (other in bidirectionalBindings) return

        bidirectionalBindings[other] = subscribable.bindBidirectional(other.asSubscribableVariable())
    }

    override fun unbindBidirectional(other: Property<T>) {
        bidirectionalBindings.remove(other)?.cancel()
    }

    override fun bind(observable: ObservableValue<out T>) {
        require(observable !== this)
        binding?.cancel()
        binding = subscribable.bind(observable)
    }

    override fun unbind() {
        binding?.cancel()
        binding = null
    }

    override fun isBound() = binding != null || bidirectionalBindings.isNotEmpty()
}
