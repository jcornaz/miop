package com.github.jcornaz.miop.property

import com.github.jcornaz.miop.distinctUntilChanged
import com.github.jcornaz.miop.receiveChannelOf
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.first

/**
 * Interface for a value holder where the value may change over time.
 *
 * The current value can be obtained with [value] and one can open a subscription in order to receive the new value when changed.
 *
 * The channel returned by [openSubscription] is "conflated" and start by the current value if any.
 */
public interface SubscribableValue<out T> {

    /**
     * Subscribes and returns a channel to receive all values, starting by the current one.
     * The resulting channel shall be [cancelled][ReceiveChannel.cancel] to unsubscribe from this subscribable.
     */
    public fun openSubscription(): ReceiveChannel<T>

    /** Returns the current value. May suspend until the value is available */
    suspend fun get(): T = openSubscription().first()
}

/**
 * Mutable version of [SubscribableValue].
 *
 * Provide ability to get and set the current value with [value].
 */
public interface SubscribableVariable<T> : SubscribableValue<T> {

    /** Set a new value */
    suspend fun set(value: T)
}

/**
 * Create an instance of [SubscribableValue] with the given [value]
 */
public fun <T> SubscribableValue(value: T): SubscribableValue<T> = object : SubscribableValue<T> {
    override suspend fun get(): T = value
    override fun openSubscription() = receiveChannelOf(value)
}

/**
 * Create an instance of [SubscribableVariable] initialized with the given [initialValue]
 */
public fun <T> SubscribableVariable(initialValue: T): SubscribableVariable<T> = object : SubscribableVariable<T> {
    private val broadcast = ConflatedBroadcastChannel(initialValue)

    override suspend fun get(): T = broadcast.value

    override suspend fun set(value: T) {
        broadcast.send(value)
    }

    override fun openSubscription() = broadcast.openSubscription().distinctUntilChanged()
}
