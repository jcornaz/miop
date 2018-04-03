package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.Channels
import com.github.jcornaz.miop.experimental.switchMap
import kotlinx.coroutines.experimental.channels.map

public object SubscribableValues {
    public fun <T1, T2, R> combine(
            value1: SubscribableValue<T1>,
            value2: SubscribableValue<T2>,
            combine: (T1, T2) -> R
    ): SubscribableValue<R> = object : SubscribableValue<R> {
        override val value: R get() = combine(value1.value, value2.value)

        override fun openSubscription() =
                Channels.combineLatest(value1.openSubscription(), value2.openSubscription()) { v1, v2 -> combine(v1, v2) }
    }
}

public fun <T, R> SubscribableValue<T>.map(transform: (T) -> R): SubscribableValue<R> = object : SubscribableValue<R> {
    override val value: R get() = transform(this@map.value)

    override fun openSubscription() = this@map.openSubscription().map { transform(it) }
}

public fun <T, R> SubscribableValue<T>.switchMap(transform: (T) -> SubscribableValue<R>): SubscribableValue<R> = object : SubscribableValue<R> {
    override val value: R get() = transform(this@switchMap.value).value

    override fun openSubscription() = this@switchMap.openSubscription().switchMap { transform(it).openSubscription() }
}

public fun <T1, T2, R> SubscribableValue<T1>.combineWith(
        other: SubscribableValue<T2>,
        combine: (T1, T2) -> R
): SubscribableValue<R> = SubscribableValues.combine(this, other, combine)
