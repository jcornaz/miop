package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.receiveChannelOf
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Interface for a value holder where the value may change over time.
 *
 * The current value can be obtained with [value] and one can open a subscription in order to receive the new value when changed.
 *
 * The channel returned by [openSubscription] is "conflated" and start by the current value if any.
 */
public interface SubscribableValue<out T> : ReadOnlyProperty<Any?, T> {

    /** Current value */
    public val value: T

    /**
     * Subscribes and returns a channel to receive all values, starting by the current one.
     * The resulting channel shall be [cancelled][ReceiveChannel.cancel] to unsubscribe from this subscribable.
     */
    public fun openSubscription(): ReceiveChannel<T>

    /**
     * Returns the current value.
     *
     * @see value
     */
    public override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

/**
 * Mutable version of [SubscribableValue].
 *
 * Provide ability to get and set the current value with [value].
 */
public interface SubscribableVariable<T> : SubscribableValue<T>, ReadWriteProperty<Any?, T> {

    /** Current value */
    public override var value: T

    public override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    /**
     * Set the current value
     */
    public override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

/**
 * Create an instance of [SubscribableValue] with the given [value]
 */
public fun <T> SubscribableValue(value: T): SubscribableValue<T> = object : SubscribableValue<T> {
    override val value: T get() = value

    override fun openSubscription() = receiveChannelOf(value)
}

/**
 * Create an instance of [SubscribableVariable] initialized with the given [initialValue]
 */
public fun <T> SubscribableVariable(initialValue: T): SubscribableVariable<T> = object : SubscribableVariable<T> {
    private val broadcast = ConflatedBroadcastChannel(initialValue)

    override var value: T
        get() = broadcast.value
        set(newValue) {
            broadcast.offer(newValue)
        }

    override fun openSubscription() = broadcast.openSubscription()
}
