package com.github.jcornaz.miop

import kotlinx.coroutines.channels.ReceiveChannel
import java.util.concurrent.TimeUnit

/**
 * Filters out elements emitted by the source that are rapidly followed by another emitted element.
 *
 * Only emit an element received if the given [timeSpan] (in millisecond) has passed without it emitting another item.
 */
public fun <E> ReceiveChannel<E>.debounce(timeSpan: Long, timeUnit: TimeUnit): ReceiveChannel<E> =
    debounce(timeUnit.toMillis(timeSpan))
