package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.receiveChannelOf
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public interface SubscribableValue<out T> : ReadOnlyProperty<Any?, T> {
    public val value: T

    public fun openSubscription(): ReceiveChannel<T>

    public override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

public interface SubscribableVariable<T> : SubscribableValue<T>, ReadWriteProperty<Any?, T> {

    public override var value: T

    public override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    public override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

public fun <T> SubscribableValue(value: T): SubscribableValue<T> = object : SubscribableValue<T> {
    override val value: T get() = value

    override fun openSubscription() = receiveChannelOf(value)
}

public fun <T> SubscribableVariable(initialValue: T): SubscribableVariable<T> = object : SubscribableVariable<T> {
    private val broadcast = ConflatedBroadcastChannel(initialValue)

    override var value: T
        get() = broadcast.value
        set(newValue) {
            broadcast.offer(newValue)
        }

    override fun openSubscription() = broadcast.openSubscription()
}
