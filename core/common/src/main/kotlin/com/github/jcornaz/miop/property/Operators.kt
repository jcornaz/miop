package com.github.jcornaz.miop.property

import com.github.jcornaz.miop.Channels
import com.github.jcornaz.miop.distinctReferenceUntilChanged
import com.github.jcornaz.miop.switchMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlin.coroutines.CoroutineContext

/**
 * Operators for [SubscribableValue]
 */
public object SubscribableValues {

    /**
     * Return a new [SubscribableValue] by combining the sources with [combine].
     *
     * If any source changed, the combined value, change accordingly, by calling [combine] again.
     */
    @ExperimentalSubscribable
    @UseExperimental(ExperimentalCoroutinesApi::class)
    public fun <T1, T2, R> combine(
        value1: SubscribableValue<T1>,
        value2: SubscribableValue<T2>,
        context: CoroutineContext = Dispatchers.Unconfined,
        combine: suspend (T1, T2) -> R
    ): SubscribableValue<R> = object : SubscribableValue<R> {
        override fun openSubscription() =
            Channels.combineLatest(value1.openSubscription(), value2.openSubscription(), context) { v1, v2 -> combine(v1, v2) }
    }
}

/**
 * Returns a [SubscribableValue] containing the results of applying the given transform function to each value of the source.
 *
 * May not emit an item [transform] returns the same reference as for the previous one.
 */
@ExperimentalSubscribable
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> SubscribableValue<T>.map(context: CoroutineContext = Dispatchers.Unconfined, transform: suspend (T) -> R): SubscribableValue<R> = object : SubscribableValue<R> {
    override fun openSubscription() = this@map.openSubscription().map(context) { transform(it) }.distinctReferenceUntilChanged()
}

/**
 * Open a subscription and apply the given [transform] for each value.
 *
 * May not emit an item [transform] returns the same reference as for the previous one.
 */
@ExperimentalSubscribable
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> SubscribableValue<T>.openSubscription(context: CoroutineContext = Dispatchers.Unconfined, transform: suspend (T) -> R): ReceiveChannel<R> =
    openSubscription().map(context) { transform(it) }.distinctReferenceUntilChanged()

/**
 * Returns a [SubscribableValue] containing backed by the latest result of [transform] which is called for each value of this subscribable value.
 */
@ExperimentalSubscribable
@UseExperimental(ExperimentalCoroutinesApi::class)
public fun <T, R> SubscribableValue<T>.switchMap(
    context: CoroutineContext = Dispatchers.Unconfined,
    transform: suspend (T) -> SubscribableValue<R>
): SubscribableValue<R> = object : SubscribableValue<R> {
    override fun openSubscription() = this@switchMap.openSubscription().switchMap(context) { transform(it).openSubscription() }
}

/**
 * Return a new [SubscribableValue] by combining the sources with [combine].
 *
 * If any source changed, the combined value, change accordingly, by calling [combine] again.
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
@ExperimentalSubscribable
public fun <T1, T2, R> SubscribableValue<T1>.combineWith(
    other: SubscribableValue<T2>,
    context: CoroutineContext = Dispatchers.Unconfined,
    combine: suspend (T1, T2) -> R
): SubscribableValue<R> = SubscribableValues.combine(this, other, context, combine)
