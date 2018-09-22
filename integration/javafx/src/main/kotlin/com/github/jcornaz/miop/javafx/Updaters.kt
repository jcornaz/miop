package com.github.jcornaz.miop.javafx

import com.github.jcornaz.miop.collection.ExperimentalCollectionEvent
import com.github.jcornaz.miop.collection.plusAssign
import com.github.jcornaz.miop.collection.toMapEvents
import javafx.beans.property.Property
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.consumes
import kotlinx.coroutines.javafx.JavaFx
import java.util.*

/**
 * Start a new job in the JavaFx thread which update the [target] with each elements received
 *
 * The result or the scope shall be cancelled in order to stop the channel
 */
public fun <T> CoroutineScope.launchFxUpdater(target: Property<in T>, source: ReceiveChannel<T>): Job =
    launch(Dispatchers.JavaFx, javafxStart()) { source.consumeEach { target.value = it } }

/**
 * Start a new job in the JavaFx thread which update the [target] with each elements received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope")
public fun <T> ReceiveChannel<T>.launchFxUpdater(target: Property<in T>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxUpdater(target, this)
}

/**
 * Start a new job in the JavaFx thread which update the [target] with each elements received
 *
 * The result or the shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope", ReplaceWith("GlobalScope.launchFxUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
public fun <T> ReceiveChannel<T>.launchFxUpdater(target: Property<in T>): Job =
    GlobalScope.launchFxUpdater(target, this)

/**
 * Start a new job in the JavaFx thread which update the [target] with each new list received
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
public fun <E> CoroutineScope.launchFxListUpdater(target: MutableList<in E>, source: ReceiveChannel<List<E>>): Job =
    launch(Dispatchers.JavaFx, javafxStart()) {
        source.consumeEach { newList ->
            when {
                target.isEmpty() -> target.addAll(newList)
                newList.isEmpty() -> target.clear()
                else -> {
                    val sourceIterator = newList.iterator()
                    val targetIterator = target.listIterator()
                    var index = 0

                    while (sourceIterator.hasNext() && targetIterator.hasNext()) {
                        val sourceElement = sourceIterator.next()

                        if (sourceElement !== targetIterator.next()) {
                            targetIterator.set(sourceElement)
                        }

                        ++index
                    }

                    if (targetIterator.hasNext()) {
                        target.subList(index, target.size).clear()
                    } else if (sourceIterator.hasNext()) {
                        sourceIterator.forEachRemaining { target.add(it) }
                    }
                }
            }
        }
    }

/**
 * Start a new job in the JavaFx thread which update the [target] with each new list received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope")
public fun <E> ReceiveChannel<List<E>>.launchFxListUpdater(target: MutableList<in E>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxListUpdater(target, this)
}

/**
 * Start a new job in the JavaFx thread which update the [target] with each new list received
 *
 * The result shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope", ReplaceWith("GlobalScope.launchFxListUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
public fun <E> ReceiveChannel<List<E>>.launchFxListUpdater(target: MutableList<in E>): Job =
    GlobalScope.launchFxListUpdater(target, this)

/**
 * Start a new job in the JavaFx thread which update the [target] with each new map received
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
@UseExperimental(ExperimentalCollectionEvent::class)
public fun <K, V> CoroutineScope.launchFxMapUpdater(target: MutableMap<in K, in V>, source: ReceiveChannel<Map<out K, V>>): Job =
    launch(Dispatchers.JavaFx, javafxStart(), onCompletion = source.consumes()) {
        target.clear()

        val initialMap = source.receive()
        target.putAll(initialMap)

        source.toMapEvents(initialMap).consumeEach { target += it }
    }

/**
 * Start a new job in the JavaFx thread which update the [target] with each new map received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope")
public fun <K, V> ReceiveChannel<Map<out K, V>>.launchFxMapUpdater(target: MutableMap<in K, in V>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxMapUpdater(target, this)
}

/**
 * Start a new job in the JavaFx thread which update the [target] with each new map received
 *
 * The result shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope", ReplaceWith("GlobalScope.launchFxMapUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
public fun <K, V> ReceiveChannel<Map<out K, V>>.launchFxMapUpdater(target: MutableMap<in K, in V>): Job =
    GlobalScope.launchFxMapUpdater(target, this)


/**
 * Start a job in the JavaFx thread which keeps up-to-date the [target] collection.
 * Order of elements is ignored. Only consider the elements and their occurrence count.
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
public fun <E> CoroutineScope.launchFxCollectionUpdater(target: MutableCollection<in E>, source: ReceiveChannel<Collection<E>>): Job =
    launch(Dispatchers.JavaFx, javafxStart()) {

        val currentElementCounts = HashMap<Any?, Int>()

        target.forEach { element ->
            currentElementCounts[element] = currentElementCounts[element]?.let { it + 1 } ?: 1
        }

        source.consumeEach { newCollection ->
            val newElementCounts = HashMap<E, Int>()

            newCollection.forEach { element ->
                val newCount = newElementCounts[element]?.let { it + 1 } ?: 1
                newElementCounts[element] = newCount

                if (newCount > currentElementCounts[element] ?: 0) {
                    target.add(element)
                    currentElementCounts[element] = currentElementCounts[element]?.let { it + 1 } ?: 1
                }
            }

            val iterator = currentElementCounts.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()

                val newCount = newElementCounts[entry.key]?.takeUnless { it <= 0 }

                if (entry.value > (newCount ?: 0)) {

                    if (target is Set<*>) {
                        if (newCount == null) target.remove(entry.key)
                    } else {
                        repeat(entry.value - (newCount ?: 0)) {
                            target.remove(entry.key)
                        }
                    }

                    if (newCount == null) {
                        iterator.remove()
                    } else {
                        entry.setValue(newCount)
                    }
                }
            }
        }
    }

/**
 * Start a job in the JavaFx thread which keeps up-to-date the [target] collection.
 * Order of elements is ignored. Only consider the elements and their occurrence count.
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope")
public fun <E> ReceiveChannel<Collection<E>>.launchFxCollectionUpdater(target: MutableCollection<in E>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxCollectionUpdater(target, this)
}

/**
 * Start a job in the JavaFx thread which keeps up-to-date the [target] collection.
 * Order of elements is ignored. Only consider the elements and their occurrence count.
 *
 * The result shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope", ReplaceWith("GlobalScope.launchFxCollectionUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
public fun <E> ReceiveChannel<Collection<E>>.launchFxCollectionUpdater(target: MutableCollection<in E>): Job =
    GlobalScope.launchFxCollectionUpdater(target, this)

/**
 * Start a new job in the JavaFx thread which update the [target] with each new set received
 *
 * The result or the scope shall be cancelled in order to cancel the channel
 */
public fun <E> CoroutineScope.launchFxSetUpdater(target: MutableSet<in E>, source: ReceiveChannel<Set<E>>): Job =
    launchFxCollectionUpdater(target, source)

/**
 * Start a new job in the JavaFx thread which update the [target] with each new set received
 *
 * The result or the [parent] shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope")
public fun <E> ReceiveChannel<Set<E>>.launchFxSetUpdater(target: MutableSet<in E>, parent: Job?): Job {
    val scope = if (parent == null) GlobalScope else CoroutineScope(parent)

    return scope.launchFxSetUpdater(target, this)
}

/**
 * Start a new job in the JavaFx thread which update the [target] with each new set received
 *
 * The result shall be cancelled in order to cancel the channel
 */
@Deprecated("launch the updater from a CoroutineScope", ReplaceWith("GlobalScope.launchFxSetUpdater(target, this)", "kotlinx.coroutines.GlobalScope"))
public fun <E> ReceiveChannel<Set<E>>.launchFxSetUpdater(target: MutableSet<in E>): Job =
    GlobalScope.launchFxSetUpdater(target, this)
