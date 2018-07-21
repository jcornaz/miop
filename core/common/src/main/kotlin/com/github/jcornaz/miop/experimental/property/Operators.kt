package com.github.jcornaz.miop.experimental.property

import com.github.jcornaz.miop.experimental.Channels
import com.github.jcornaz.miop.experimental.distinctReferenceUntilChanged
import com.github.jcornaz.miop.experimental.switchMap
import com.github.jcornaz.miop.experimental.transform
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.map

/**
 * Operators for [SubscribableValue]
 */
public object SubscribableValues {

    /**
     * Return a new [SubscribableValue] by combining the sources with [combine].
     *
     * If any source changed, the combined value, change accordingly, by calling [combine] again.
     */
    public fun <T1, T2, R> combine(
            value1: SubscribableValue<T1>,
            value2: SubscribableValue<T2>,
            combine: suspend (T1, T2) -> R
    ): SubscribableValue<R> = object : SubscribableValue<R> {
        override fun openSubscription() =
                Channels.combineLatest(value1.openSubscription(), value2.openSubscription()) { v1, v2 -> combine(v1, v2) }
    }
}

/**
 * Returns a [SubscribableValue] containing the results of applying the given transform function to each value of the source.
 *
 * May not emit an item [transform] returns the same reference as for the previous one.
 */
public fun <T, R> SubscribableValue<T>.map(transform: suspend (T) -> R): SubscribableValue<R> = object : SubscribableValue<R> {
    override fun openSubscription() = this@map.openSubscription().map { transform(it) }.distinctReferenceUntilChanged()
}

public fun <S1, S2, A1, A2> StateStore<S1, A1>.map(
    transformState: (S1) -> S2,
    transformAction: (A2) -> A1
): StateStore<S2, A2> = TODO()

/**
 * Open a subscription and apply the given [transform] for each value.
 *
 * May not emit an item [transform] returns the same reference as for the previous one.
 */
public fun <T, R> SubscribableValue<T>.openSubscription(transform: suspend (T) -> R): ReceiveChannel<R> =
    openSubscription().map { transform(it) }.distinctReferenceUntilChanged()

/**
 * Returns a [SubscribableValue] containing backed by the latest result of [transform] which is called for each value of this subscribable value.
 */
public fun <T, R> SubscribableValue<T>.switchMap(transform: suspend (T) -> SubscribableValue<R>): SubscribableValue<R> = object : SubscribableValue<R> {
    override fun openSubscription() = this@switchMap.openSubscription().switchMap { transform(it).openSubscription() }
}

/**
 * Return a new [SubscribableValue] by combining the sources with [combine].
 *
 * If any source changed, the combined value, change accordingly, by calling [combine] again.
 */
public fun <T1, T2, R> SubscribableValue<T1>.combineWith(
        other: SubscribableValue<T2>,
        combine: suspend (T1, T2) -> R
): SubscribableValue<R> = SubscribableValues.combine(this, other, combine)
